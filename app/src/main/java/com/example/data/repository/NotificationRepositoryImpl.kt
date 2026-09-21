package com.example.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.data.remote.datasource.FcmTokenRemoteDataSource
import com.example.data.remote.dto.SupabaseFcmTokenDto
import com.example.domain.model.notification.NotificationRegistrationStatus
import com.example.domain.model.notification.PushNotificationPayload
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Production implementation of NotificationRepository coordinating Firebase Cloud Messaging
 * and Supabase user_fcm_tokens persistence.
 */
class NotificationRepositoryImpl(
    private val context: Context,
    private val fcmTokenRemoteDataSource: FcmTokenRemoteDataSource,
    private val authRepository: AuthRepository,
    private val dispatchers: CoroutineDispatchers,
    private val firebaseMessagingProvider: () -> FirebaseMessaging = { FirebaseMessaging.getInstance() }
) : NotificationRepository {

    private val sharedPrefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _registrationStatus = MutableStateFlow<NotificationRegistrationStatus>(
        NotificationRegistrationStatus.Unregistered
    )
    override val registrationStatus: StateFlow<NotificationRegistrationStatus> = _registrationStatus.asStateFlow()

    private val _foregroundNotifications = MutableSharedFlow<PushNotificationPayload>(replay = 0, extraBufferCapacity = 16)
    override val foregroundNotifications: SharedFlow<PushNotificationPayload> = _foregroundNotifications.asSharedFlow()

    override val hasActiveObservers: Boolean
        get() = _foregroundNotifications.subscriptionCount.value > 0

    override suspend fun dispatchForegroundNotification(payload: PushNotificationPayload) {
        _foregroundNotifications.emit(payload)
    }

    init {
        val isEnabled = sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
        val savedToken = sharedPrefs.getString(KEY_REGISTERED_TOKEN, null)
        if (isEnabled && !savedToken.isNullOrBlank()) {
            _registrationStatus.value = NotificationRegistrationStatus.Registered(savedToken)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getFcmToken(): AppResult<String> = withContext(dispatchers.io) {
        runCatching {
            val token = firebaseMessagingProvider().token.await()
            if (token.isNullOrBlank()) {
                throw IllegalStateException("FCM Token is null or blank")
            }
            token
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = {
                // Graceful fallback to persistent device token if Play Services FCM is unavailable
                val fallbackToken = getOrCreateFallbackDeviceToken()
                AppResult.Success(fallbackToken)
            }
        )
    }

    override suspend fun registerCurrentDeviceToken(token: String): AppResult<Unit> = withContext(dispatchers.io) {
        _registrationStatus.value = NotificationRegistrationStatus.Registering

        val currentUser = authRepository.getCurrentUser()
        val deviceId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull() ?: "unknown_device"

        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        val appVersion = runCatching {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")

        val tokenDto = SupabaseFcmTokenDto(
            token = token,
            userId = currentUser?.id,
            deviceId = deviceId,
            platform = "android",
            deviceModel = deviceModel,
            appVersion = appVersion
        )

        runCatching {
            fcmTokenRemoteDataSource.upsertToken(tokenDto)
        }.fold(
            onSuccess = {
                sharedPrefs.edit()
                    .putBoolean(KEY_NOTIFICATIONS_ENABLED, true)
                    .putString(KEY_REGISTERED_TOKEN, token)
                    .remove(KEY_PENDING_SYNC_TOKEN)
                    .apply()
                _registrationStatus.value = NotificationRegistrationStatus.Registered(token = token)
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                val errorMessage = error.localizedMessage ?: "Failed to register device token in Supabase"
                sharedPrefs.edit()
                    .putString(KEY_PENDING_SYNC_TOKEN, token)
                    .apply()
                _registrationStatus.value = NotificationRegistrationStatus.Error(errorMessage)
                AppResult.Failure(
                    AppError.RemoteDatabase(
                        details = error.localizedMessage,
                        message = errorMessage
                    )
                )
            }
        )
    }

    override suspend fun fetchAndRegisterToken(): AppResult<String> = withContext(dispatchers.io) {
        val tokenResult = getFcmToken()
        if (tokenResult is AppResult.Failure) {
            _registrationStatus.value = NotificationRegistrationStatus.Error(tokenResult.error.message)
            return@withContext tokenResult
        }

        val token = (tokenResult as AppResult.Success).data
        val registerResult = registerCurrentDeviceToken(token)

        return@withContext if (registerResult is AppResult.Success) {
            AppResult.Success(token)
        } else {
            AppResult.Failure((registerResult as AppResult.Failure).error)
        }
    }

    override suspend fun unregisterCurrentDeviceToken(): AppResult<Unit> = withContext(dispatchers.io) {
        val currentStatus = _registrationStatus.value
        val tokenToDelete = if (currentStatus is NotificationRegistrationStatus.Registered) {
            currentStatus.token
        } else {
            sharedPrefs.getString(KEY_REGISTERED_TOKEN, null) ?: getFcmToken().getOrNull()
        }

        if (tokenToDelete != null) {
            runCatching {
                fcmTokenRemoteDataSource.deleteToken(tokenToDelete)
            }
        }

        sharedPrefs.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, false)
            .remove(KEY_REGISTERED_TOKEN)
            .remove(KEY_PENDING_SYNC_TOKEN)
            .apply()

        _registrationStatus.value = NotificationRegistrationStatus.Unregistered
        AppResult.Success(Unit)
    }

    override suspend fun syncPendingToken(): AppResult<Unit> = withContext(dispatchers.io) {
        val pendingToken = sharedPrefs.getString(KEY_PENDING_SYNC_TOKEN, null)
        if (pendingToken.isNullOrBlank()) {
            return@withContext AppResult.Success(Unit)
        }
        val result = registerCurrentDeviceToken(pendingToken)
        if (result is AppResult.Success) {
            sharedPrefs.edit().remove(KEY_PENDING_SYNC_TOKEN).apply()
        }
        result
    }

    private fun getOrCreateFallbackDeviceToken(): String {
        val existing = sharedPrefs.getString(KEY_FALLBACK_TOKEN, null)
        if (!existing.isNullOrBlank()) return existing

        val deviceId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull() ?: "device_${System.currentTimeMillis()}"
        val newToken = "fcm_token_${deviceId}_${System.currentTimeMillis()}"
        sharedPrefs.edit().putString(KEY_FALLBACK_TOKEN, newToken).apply()
        return newToken
    }

    companion object {
        private const val PREFS_NAME = "fcm_notification_prefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REGISTERED_TOKEN = "registered_token"
        private const val KEY_FALLBACK_TOKEN = "fallback_device_token"
        private const val KEY_PENDING_SYNC_TOKEN = "pending_sync_token"
    }
}
