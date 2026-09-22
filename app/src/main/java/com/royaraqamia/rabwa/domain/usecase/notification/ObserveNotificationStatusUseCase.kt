package com.royaraqamia.rabwa.domain.usecase.notification

import com.royaraqamia.rabwa.domain.model.notification.NotificationRegistrationStatus
import com.royaraqamia.rabwa.domain.repository.NotificationRepository
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
