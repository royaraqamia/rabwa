package com.example.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.notification.NotificationChannelManager
import com.example.domain.usecase.notification.HandleIncomingNotificationUseCase
import com.example.presentation.ArchitectureViewModelFactory
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Android Background Service handling Firebase Cloud Messaging lifecycle and incoming payloads.
 */
class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handleIncomingNotificationUseCase = HandleIncomingNotificationUseCase()

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Automatically sync new token to Supabase if a user session exists
        serviceScope.launch {
            runCatching {
                val factory = ArchitectureViewModelFactory.getInstance(applicationContext)
                factory.registerPushTokenUseCase(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val rawTitle = remoteMessage.notification?.title
        val rawBody = remoteMessage.notification?.body
        val rawData = remoteMessage.data

        val payload = handleIncomingNotificationUseCase(rawTitle, rawBody, rawData) ?: return

        serviceScope.launch {
            try {
                val factory = ArchitectureViewModelFactory.getInstance(applicationContext)
                val notificationRepo = factory.notificationRepository
                if (notificationRepo.hasActiveObservers) {
                    notificationRepo.dispatchForegroundNotification(payload)
                } else {
                    showSystemNotification(payload.title, payload.body, payload.channelId)
                }
            } catch (e: Exception) {
                // Secure fallback to system tray if database or factory fails to resolve
                showSystemNotification(payload.title, payload.body, payload.channelId)
            }
        }
    }

    private fun showSystemNotification(title: String, body: String, customChannelId: String?) {
        NotificationChannelManager.ensureDefaultChannel(applicationContext)

        val channelId = customChannelId ?: NotificationChannelManager.DEFAULT_CHANNEL_ID

        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_BODY, body)
            putExtra(EXTRA_NOTIFICATION_TIME, System.currentTimeMillis())
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId, // Unique request code per notification prevents intent bundle overwrites in the tray
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, notification)
    }

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 1010
        const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
        const val EXTRA_NOTIFICATION_BODY = "extra_notification_body"
        const val EXTRA_NOTIFICATION_TIME = "extra_notification_time"
    }
}
