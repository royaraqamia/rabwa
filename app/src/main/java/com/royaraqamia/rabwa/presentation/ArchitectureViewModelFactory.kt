package com.royaraqamia.rabwa.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.royaraqamia.rabwa.core.dispatcher.DefaultDispatchers
import com.royaraqamia.rabwa.data.local.AppDatabase
import com.royaraqamia.rabwa.data.network.ConnectivityMonitorImpl
import com.royaraqamia.rabwa.data.remote.datasource.SupabaseAuthRemoteDataSource
import com.royaraqamia.rabwa.data.remote.datasource.SupabaseDatabaseRemoteDataSource
import com.royaraqamia.rabwa.data.remote.datasource.SupabaseStorageRemoteDataSource
import com.royaraqamia.rabwa.data.repository.ArchitectureRepositoryImpl
import com.royaraqamia.rabwa.data.repository.AuthRepositoryImpl
import com.royaraqamia.rabwa.data.repository.RemoteSyncRepositoryImpl
import com.royaraqamia.rabwa.data.repository.StorageRepositoryImpl
import com.royaraqamia.rabwa.domain.usecase.AddArchitectureRecordUseCase
import com.royaraqamia.rabwa.domain.usecase.ClearRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.GetLayerHealthUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveNetworkStatusUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveSyncSummaryUseCase
import com.royaraqamia.rabwa.domain.usecase.SyncOfflineRecordsUseCase
import com.royaraqamia.rabwa.core.network.SupabaseClientProvider
import com.royaraqamia.rabwa.data.local.session.SharedPreferencesSessionManager
import com.royaraqamia.rabwa.domain.usecase.auth.RefreshSessionUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.RestoreSessionUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.GetCurrentUserUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.ObserveAuthStateUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.ResetPasswordUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignInWithEmailUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignInWithGoogleUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignOutUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignUpWithEmailUseCase
import com.royaraqamia.rabwa.domain.usecase.storage.DeleteStorageFileUseCase
import com.royaraqamia.rabwa.domain.usecase.storage.GetStorageFileUrlUseCase
import com.royaraqamia.rabwa.domain.usecase.storage.ListStorageFilesUseCase
import com.royaraqamia.rabwa.domain.usecase.storage.UploadStorageFileUseCase
import com.royaraqamia.rabwa.presentation.auth.AuthViewModel
import com.royaraqamia.rabwa.presentation.storage.StorageViewModel
import com.royaraqamia.rabwa.data.remote.datasource.SupabaseFcmTokenRemoteDataSource
import com.royaraqamia.rabwa.data.repository.NotificationRepositoryImpl
import com.royaraqamia.rabwa.domain.repository.NotificationRepository
import com.royaraqamia.rabwa.domain.usecase.notification.HandleIncomingNotificationUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.ObserveNotificationStatusUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.RegisterPushTokenUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.UnregisterPushTokenUseCase
import com.royaraqamia.rabwa.presentation.notification.NotificationViewModel

class ArchitectureViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val dispatchers = DefaultDispatchers()

    private val authRepository by lazy {
        SupabaseClientProvider.setSessionManager(SharedPreferencesSessionManager(context.applicationContext))
        AuthRepositoryImpl(
            remoteDataSource = SupabaseAuthRemoteDataSource(),
            dispatchers = dispatchers
        )
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(
            context = context.applicationContext,
            fcmTokenRemoteDataSource = SupabaseFcmTokenRemoteDataSource(),
            authRepository = authRepository,
            dispatchers = dispatchers
        )
    }

    val registerPushTokenUseCase by lazy { RegisterPushTokenUseCase(notificationRepository) }
    val unregisterPushTokenUseCase by lazy { UnregisterPushTokenUseCase(notificationRepository) }
    val observeNotificationStatusUseCase by lazy { ObserveNotificationStatusUseCase(notificationRepository) }
    val handleIncomingNotificationUseCase by lazy { HandleIncomingNotificationUseCase() }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getInstance(context)
        val connectivityMonitor = ConnectivityMonitorImpl(
            context = context,
            dispatchers = dispatchers
        )

        if (modelClass.isAssignableFrom(ArchitectureViewModel::class.java)) {
            val repository = ArchitectureRepositoryImpl(
                dao = database.architectureDao(),
                dispatchers = dispatchers,
                connectivityMonitor = connectivityMonitor
            )

            return ArchitectureViewModel(
                getLayerHealthUseCase = GetLayerHealthUseCase(repository),
                addArchitectureRecordUseCase = AddArchitectureRecordUseCase(repository),
                observeRecordsUseCase = ObserveRecordsUseCase(repository),
                clearRecordsUseCase = ClearRecordsUseCase(repository),
                observeNetworkStatusUseCase = ObserveNetworkStatusUseCase(connectivityMonitor),
                syncOfflineRecordsUseCase = SyncOfflineRecordsUseCase(repository),
                observeSyncSummaryUseCase = ObserveSyncSummaryUseCase(repository),
                dispatchers = dispatchers
            ) as T
        }

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                observeAuthStateUseCase = ObserveAuthStateUseCase(authRepository),
                getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
                signInWithEmailUseCase = SignInWithEmailUseCase(authRepository),
                signUpWithEmailUseCase = SignUpWithEmailUseCase(authRepository),
                signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository),
                signOutUseCase = SignOutUseCase(authRepository),
                resetPasswordUseCase = ResetPasswordUseCase(authRepository),
                restoreSessionUseCase = RestoreSessionUseCase(authRepository),
                refreshSessionUseCase = RefreshSessionUseCase(authRepository),
                registerPushTokenUseCase = registerPushTokenUseCase,
                unregisterPushTokenUseCase = unregisterPushTokenUseCase
            ) as T
        }

        if (modelClass.isAssignableFrom(StorageViewModel::class.java)) {
            val storageRepository = StorageRepositoryImpl(
                remoteDataSource = SupabaseStorageRemoteDataSource(),
                dispatchers = dispatchers
            )
            return StorageViewModel(
                uploadStorageFileUseCase = UploadStorageFileUseCase(storageRepository),
                getStorageFileUrlUseCase = GetStorageFileUrlUseCase(storageRepository),
                deleteStorageFileUseCase = DeleteStorageFileUseCase(storageRepository),
                listStorageFilesUseCase = ListStorageFilesUseCase(storageRepository)
            ) as T
        }

        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(
                registerPushTokenUseCase = registerPushTokenUseCase,
                unregisterPushTokenUseCase = unregisterPushTokenUseCase,
                observeNotificationStatusUseCase = observeNotificationStatusUseCase,
                dispatchers = dispatchers
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    companion object {
        @Volatile
        private var instance: ArchitectureViewModelFactory? = null

        fun getInstance(context: Context): ArchitectureViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: ArchitectureViewModelFactory(context.applicationContext).also { instance = it }
            }
        }
    }
}

