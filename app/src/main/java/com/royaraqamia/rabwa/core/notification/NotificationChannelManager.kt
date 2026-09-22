package com.royaraqamia.rabwa.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.royaraqamia.rabwa.R

/**
 * Enterprise Notification Channel Manager for Android O+.
 * Configures distinct channels with localized names and descriptions.
 */
object NotificationChannelManager {
    const val DEFAULT_CHANNEL_ID = "rabwah_general_notifications"

    fun ensureDefaultChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channelName = context.getString(R.string.notifications_channel_name)
            val channelDescription = context.getString(R.string.notifications_channel_desc)

            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }
}
