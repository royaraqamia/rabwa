package com.royaraqamia.rabwa.domain.model.notification

/**
 * Domain model representing a device push notification registration token.
 */
data class DevicePushToken(
    val token: String,
    val userId: String,
    val deviceId: String,
    val platform: String = "android",
    val deviceModel: String,
    val appVersion: String,
    val updatedAt: String? = null
)
