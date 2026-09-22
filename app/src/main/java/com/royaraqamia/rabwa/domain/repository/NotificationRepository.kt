package com.royaraqamia.rabwa.domain.repository

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.notification.DevicePushToken
import com.royaraqamia.rabwa.domain.model.notification.NotificationRegistrationStatus
import com.royaraqamia.rabwa.domain.model.notification.PushNotificationPayload
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
