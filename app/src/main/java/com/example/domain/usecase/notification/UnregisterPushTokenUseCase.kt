package com.example.domain.usecase.notification

import com.example.core.result.AppResult
import com.example.domain.repository.NotificationRepository

/**
 * UseCase for unregistering the current device FCM token on sign-out.
 */
class UnregisterPushTokenUseCase(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return notificationRepository.unregisterCurrentDeviceToken()
    }
}
