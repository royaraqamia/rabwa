package com.example.domain.usecase.notification

import com.example.domain.model.notification.NotificationRegistrationStatus
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase for observing the reactive notification registration status.
 */
class ObserveNotificationStatusUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): StateFlow<NotificationRegistrationStatus> {
        return notificationRepository.registrationStatus
    }
}
