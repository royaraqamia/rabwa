package com.royaraqamia.rabwa.domain.usecase.notification

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.repository.NotificationRepository

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
