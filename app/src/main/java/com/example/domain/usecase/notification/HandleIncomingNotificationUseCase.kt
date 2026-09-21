package com.example.domain.usecase.notification

import com.example.domain.model.notification.PushNotificationPayload

/**
 * UseCase to sanitize and validate incoming raw FCM push notification payloads.
 * Enforces Zero-Trust sanitization before presentation or processing.
 */
class HandleIncomingNotificationUseCase {

    operator fun invoke(
        rawTitle: String?,
        rawBody: String?,
        rawData: Map<String, String>
    ): PushNotificationPayload? {
        val sanitizedTitle = rawTitle?.trim()?.take(MAX_TITLE_LENGTH)
            ?: rawData["title"]?.trim()?.take(MAX_TITLE_LENGTH)

        val sanitizedBody = rawBody?.trim()?.take(MAX_BODY_LENGTH)
            ?: rawData["body"]?.trim()?.take(MAX_BODY_LENGTH)

        // If both title and body are completely empty or null, discard invalid payload
        if (sanitizedTitle.isNullOrBlank() && sanitizedBody.isNullOrBlank()) {
            return null
        }

        // Sanitize data map
        val sanitizedData = rawData.filter { it.key.isNotBlank() && it.value.isNotBlank() }
        val channelId = sanitizedData["channel_id"] ?: sanitizedData["channel"]

        return PushNotificationPayload(
            title = sanitizedTitle.orEmpty(),
            body = sanitizedBody.orEmpty(),
            data = sanitizedData,
            channelId = channelId
        )
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 120
        private const val MAX_BODY_LENGTH = 500
    }
}
