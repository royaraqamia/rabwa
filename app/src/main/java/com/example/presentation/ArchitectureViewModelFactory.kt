package com.example.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.dispatcher.DefaultDispatchers
import com.example.data.local.AppDatabase
import com.example.data.network.ConnectivityMonitorImpl
import com.example.data.remote.datasource.SupabaseAuthRemoteDataSource
import com.example.data.remote.datasource.SupabaseDatabaseRemoteDataSource
import com.example.data.remote.datasource.SupabaseStorageRemoteDataSource
import com.example.data.repository.ArchitectureRepositoryImpl
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.RemoteSyncRepositoryImpl
import com.example.data.repository.StorageRepositoryImpl
import com.example.domain.usecase.AddArchitectureRecordUseCase
import com.example.domain.usecase.ClearRecordsUseCase
import com.example.domain.usecase.GetLayerHealthUseCase
import com.example.domain.usecase.ObserveNetworkStatusUseCase
import com.example.domain.usecase.ObserveRecordsUseCase
import com.example.domain.usecase.ObserveSyncSummaryUseCase
import com.example.domain.usecase.SyncOfflineRecordsUseCase
import com.example.core.network.SupabaseClientProvider
import com.example.data.local.session.SharedPreferencesSessionManager
import com.example.domain.usecase.auth.RefreshSessionUseCase
import com.example.domain.usecase.auth.RestoreSessionUseCase
import com.example.domain.usecase.auth.GetCurrentUserUseCase
import com.example.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.domain.usecase.auth.ResetPasswordUseCase
import com.example.domain.usecase.auth.SignInWithEmailUseCase
import com.example.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.domain.usecase.auth.SignOutUseCase
import com.example.domain.usecase.auth.SignUpWithEmailUseCase
import com.example.domain.usecase.storage.DeleteStorageFileUseCase
import com.example.domain.usecase.storage.GetStorageFileUrlUseCase
import com.example.domain.usecase.storage.ListStorageFilesUseCase
import com.example.domain.usecase.storage.UploadStorageFileUseCase
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.storage.StorageViewModel
import com.example.data.remote.datasource.SupabaseFcmTokenRemoteDataSource
import com.example.data.repository.NotificationRepositoryImpl
import com.example.domain.repository.NotificationRepository
import com.example.domain.usecase.notification.HandleIncomingNotificationUseCase
import com.example.domain.usecase.notification.ObserveNotificationStatusUseCase
import com.example.domain.usecase.notification.RegisterPushTokenUseCase
import com.example.domain.usecase.notification.UnregisterPushTokenUseCase
import com.example.presentation.notification.NotificationViewModel

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

