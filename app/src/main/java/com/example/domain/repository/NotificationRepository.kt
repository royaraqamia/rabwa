package com.example.domain.repository

import com.example.core.result.AppResult
import com.example.domain.model.notification.DevicePushToken
import com.example.domain.model.notification.NotificationRegistrationStatus
import com.example.domain.model.notification.PushNotificationPayload
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain repository contract for Push Notifications (Supabase + FCM).
 */
interface NotificationRepository {
    val registrationStatus: StateFlow<NotificationRegistrationStatus>
    val foregroundNotifications: SharedFlow<PushNotificationPayload>
    val hasActiveObservers: Boolean

    suspend fun registerCurrentDeviceToken(token: String): AppResult<Unit>
    suspend fun unregisterCurrentDeviceToken(): AppResult<Unit>
    suspend fun getFcmToken(): AppResult<String>
    suspend fun fetchAndRegisterToken(): AppResult<String>
    suspend fun syncPendingToken(): AppResult<Unit>
    suspend fun dispatchForegroundNotification(payload: PushNotificationPayload)
}
