package com.example.presentation.notification

import com.example.domain.model.notification.NotificationRegistrationStatus

/**
 * UI State for Push Notification settings & diagnostics.
 */
data class NotificationUiState(
    val hasSystemPermission: Boolean = false,
    val isNotificationsEnabled: Boolean = false,
    val registrationStatus: NotificationRegistrationStatus = NotificationRegistrationStatus.Unregistered,
    val currentToken: String? = null,
    val isSyncing: Boolean = false,
    val userMessage: String? = null,
    val isError: Boolean = false
)
