package com.royaraqamia.rabwa.domain.model.notification

/**
 * Status of device push notification registration with Supabase backend.
 */
sealed interface NotificationRegistrationStatus {
    object Unregistered : NotificationRegistrationStatus
    object Registering : NotificationRegistrationStatus
    data class Registered(val token: String, val lastRegisteredAt: Long = System.currentTimeMillis()) : NotificationRegistrationStatus
    data class Error(val message: String) : NotificationRegistrationStatus
}
