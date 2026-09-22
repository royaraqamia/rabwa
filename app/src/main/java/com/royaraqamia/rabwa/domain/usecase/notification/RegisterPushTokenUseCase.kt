package com.royaraqamia.rabwa.domain.usecase.notification

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.repository.NotificationRepository

/**
 * UseCase for registering a device FCM token with the Supabase backend.
 */
class RegisterPushTokenUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(token: String? = null): AppResult<String> {
        return if (token != null && token.isNotBlank()) {
            val registerResult = notificationRepository.registerCurrentDeviceToken(token)
            if (registerResult is AppResult.Success) {
                AppResult.Success(token)
            } else {
                AppResult.Failure((registerResult as AppResult.Failure).error)
            }
        } else {
            notificationRepository.fetchAndRegisterToken()
        }
    }

    suspend fun syncPendingToken(): AppResult<Unit> {
        return notificationRepository.syncPendingToken()
    }
}
