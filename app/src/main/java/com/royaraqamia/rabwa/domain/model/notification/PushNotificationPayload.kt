package com.royaraqamia.rabwa.domain.model.notification

/**
 * Domain model representing a validated incoming push notification payload.
 */
data class PushNotificationPayload(
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val channelId: String? = null
)
