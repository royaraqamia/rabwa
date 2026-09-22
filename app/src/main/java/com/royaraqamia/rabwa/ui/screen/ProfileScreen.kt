package com.royaraqamia.rabwa.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.royaraqamia.rabwa.ui.icon.AlertTriangle
import com.royaraqamia.rabwa.ui.icon.CheckCircle2
import com.royaraqamia.rabwa.ui.icon.CloudCheck
import com.royaraqamia.rabwa.ui.icon.Keyboard
import com.royaraqamia.rabwa.ui.icon.Lock
import com.royaraqamia.rabwa.ui.icon.LogIn
import com.royaraqamia.rabwa.ui.icon.LogOut
import com.royaraqamia.rabwa.ui.icon.LucideIcons
import com.royaraqamia.rabwa.ui.icon.Mail
import com.royaraqamia.rabwa.ui.icon.Moon
import com.royaraqamia.rabwa.ui.icon.Sun
import com.royaraqamia.rabwa.ui.icon.User
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royaraqamia.rabwa.R
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.presentation.auth.AuthViewModel
import com.royaraqamia.rabwa.presentation.auth.state.AuthUiState
import com.royaraqamia.rabwa.ui.component.AppCircularProgressIndicator
import com.royaraqamia.rabwa.ui.theme.ThemeMode
import com.royaraqamia.rabwa.ui.theme.spacing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royaraqamia.rabwa.domain.model.notification.NotificationRegistrationStatus
import com.royaraqamia.rabwa.presentation.notification.NotificationViewModel
import com.royaraqamia.rabwa.presentation.qr.QrScannerViewModel
import com.royaraqamia.rabwa.ui.icon.Bell
import com.royaraqamia.rabwa.ui.icon.BellOff
import com.royaraqamia.rabwa.ui.icon.QrCode
import com.royaraqamia.rabwa.ui.icon.RefreshCw
import com.royaraqamia.rabwa.ui.screen.qr.QrScannerDialog
import com.royaraqamia.rabwa.ui.component.profile.ProfileAvatar
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel? = null,
    qrScannerViewModel: QrScannerViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val actionMessage by authViewModel.actionMessage.collectAsStateWithLifecycle()
    val isAvatarUploading by authViewModel.isAvatarUploading.collectAsStateWithLifecycle()
    val notificationState = notificationViewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val qrScannerUiState by qrScannerViewModel.uiState.collectAsStateWithLifecycle()

    var showPermissionRationaleDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Notification permission launcher for Android 13+ (TIRAMISU)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationViewModel?.updatePermissionState(isGranted)
        if (isGranted) {
            notificationViewModel?.registerDeviceToken()
        }
    }

    // Check notification permission state initially
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        notificationViewModel?.updatePermissionState(hasPermission)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { qrScannerViewModel.openScanner() },
                        modifier = Modifier.testTag("profile_app_bar_qr_scan_button")
                    ) {
                        Icon(
                            imageVector = LucideIcons.QrCode,
                            contentDescription = stringResource(R.string.qr_scanner_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                // Action / Error Messages
                AnimatedVisibility(
                    visible = actionMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    actionMessage?.let { msg ->
                        ProfileMessageBanner(
                            message = msg,
                            isError = false,
                            onDismiss = { authViewModel.clearMessages() }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    errorMessage?.let { err ->
                        ProfileMessageBanner(
                            message = err,
                            isError = true,
                            onDismiss = { authViewModel.clearMessages() }
                        )
                    }
                }

                when (val state = authUiState) {
                    is AuthUiState.Authenticated -> {
                        AuthenticatedProfileContent(
                            user = state.user,
                            isUploading = isAvatarUploading,
                            onAvatarCaptured = { bytes -> authViewModel.updateAvatarFromBytes(bytes) },
                            onSignOut = {
                                authViewModel.signOut()
                                onNavigateToLogin()
                            }
                        )
                    }
                    is AuthUiState.Unauthenticated -> {
                        GuestProfileContent(
                            isUploading = isAvatarUploading,
                            onAvatarCaptured = { bytes -> authViewModel.updateAvatarFromBytes(bytes) },
                            onNavigateToLogin = onNavigateToLogin
                        )
                    }
                    is AuthUiState.Loading -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_loading_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.extraLarge),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AppCircularProgressIndicator(
                                    size = 36.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    testTag = "profile_loading_indicator"
                                )
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                                Text(
                                    text = stringResource(R.string.loading_generic),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    is AuthUiState.Error -> {
                        GuestProfileContent(
                            onNavigateToLogin = onNavigateToLogin
                        )
                    }
                }

                // Push Notifications Card (Supabase + FCM)
                if (notificationViewModel != null && notificationState != null) {
                    NotificationSettingsCard(
                        state = notificationState,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    notificationViewModel.registerDeviceToken()
                                } else {
                                    showPermissionRationaleDialog = true
                                }
                            } else {
                                notificationViewModel.registerDeviceToken()
                            }
                        },
                        onRegisterToken = { notificationViewModel.registerDeviceToken() },
                        onUnregisterToken = { notificationViewModel.unregisterDeviceToken() },
                        onDismissMessage = { notificationViewModel.clearUserMessage() }
                    )
                }

                if (showPermissionRationaleDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionRationaleDialog = false },
                        title = {
                            Text(
                                text = "تمكين الإشعارات الفورية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = "نحتاج إلى إذن الإشعارات لإرسال تنبيهات فورية، وتحديثات هامة عن حالة النظام، وتقارير المزامنة فور حدوثها.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    showPermissionRationaleDialog = false
                                }
                            ) {
                                Text("السماح")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showPermissionRationaleDialog = false }
                            ) {
                                Text("ليس الآن")
                            }
                        }
                    )
                }

                // Theme Settings Card (Light / Dark Mode Toggle Switch)
                ThemeSettingsCard(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange
                )

                // Keyboard Settings Card
                KeyboardSettingsCard()

                // App Version Number Only
                AppVersionFooter()

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }

            // QR Code Scanner Dialog Modal
            if (qrScannerUiState.isScannerOpen) {
                QrScannerDialog(
                    viewModel = qrScannerViewModel,
                    onDismiss = { qrScannerViewModel.closeScanner() }
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedProfileContent(
    user: AuthUser,
    isUploading: Boolean = false,
    onAvatarCaptured: (ByteArray) -> Unit = {},
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("authenticated_profile_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                avatarUrl = user.avatarUrl,
                userDisplayName = null,
                userEmail = user.email,
                isUploading = isUploading,
                onAvatarCaptured = onAvatarCaptured
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Status Badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.testTag("auth_status_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = stringResource(R.string.auth_status_authenticated),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            val displayEmail = user.email?.takeIf { it.isNotBlank() } ?: "مستخدم مسجل"
            Text(
                text = displayEmail,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("user_email_text")
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Account details
            ProfileDetailRow(
                icon = LucideIcons.Mail,
                label = stringResource(R.string.auth_email_label),
                value = displayEmail
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            ProfileDetailRow(
                icon = LucideIcons.Lock,
                label = stringResource(R.string.auth_provider_label),
                value = user.provider.name
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            ProfileDetailRow(
                icon = LucideIcons.CloudCheck,
                label = stringResource(R.string.auth_cloud_sync_active),
                value = "مفعلة وتعمل تلقائياً"
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            OutlinedButton(
                onClick = onSignOut,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = LucideIcons.LogOut,
                    contentDescription = stringResource(R.string.auth_action_logout),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(stringResource(R.string.auth_action_logout), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GuestProfileContent(
    isUploading: Boolean = false,
    onAvatarCaptured: (ByteArray) -> Unit = {},
    onNavigateToLogin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guest_profile_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                avatarUrl = null,
                userDisplayName = "زائر",
                userEmail = "guest@example.com",
                isUploading = isUploading,
                onAvatarCaptured = onAvatarCaptured
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            Button(
                onClick = onNavigateToLogin,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_navigation_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = LucideIcons.LogIn,
                    contentDescription = stringResource(R.string.auth_action_login),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(R.string.auth_action_login),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileMessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val bgColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    val icon = if (isError) LucideIcons.AlertTriangle else LucideIcons.CheckCircle2

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isError) "خطأ" else "نجاح",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Text("✕", color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KeyboardSettingsCard() {
    val context = LocalContext.current
    val defaultImeId = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    val isArabicKeyboardActive = defaultImeId?.contains(context.packageName) == true

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isArabicKeyboardActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = LucideIcons.Keyboard,
                        contentDescription = "Keyboard Settings",
                        tint = if (isArabicKeyboardActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "إعدادات لوحة المفاتيح",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabicKeyboardActive) "لوحة المفاتيح العربية مفعلة حالياً" else "إضغط لاختيار لوحة المفاتيح وتفعيلها",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isArabicKeyboardActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    state: com.royaraqamia.rabwa.presentation.notification.NotificationUiState,
    onRequestPermission: () -> Unit,
    onRegisterToken: () -> Unit = {},
    onUnregisterToken: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val isEnabled = state.isNotificationsEnabled
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_settings_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isEnabled) LucideIcons.Bell else LucideIcons.BellOff,
                                contentDescription = stringResource(R.string.notifications_title),
                                tint = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.notifications_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) {
                                stringResource(R.string.notifications_status_enabled)
                            } else {
                                stringResource(R.string.notifications_status_disabled)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onRequestPermission()
                        } else {
                            onUnregisterToken()
                        }
                    },
                    modifier = Modifier.testTag("notification_switch"),
                    thumbContent = {
                        Icon(
                            imageVector = if (isEnabled) LucideIcons.Bell else LucideIcons.BellOff,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
            }

            val context = LocalContext.current
            val isSystemPermitted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                state.hasSystemPermission
            } else {
                true
            }

            if (state.isNotificationsEnabled && !isSystemPermitted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.small)
                        .clickable {
                            runCatching {
                                val intent = Intent().apply {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    } else {
                                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                        putExtra("app_package", context.packageName)
                                        putExtra("app_uid", context.applicationInfo.uid)
                                    }
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            imageVector = LucideIcons.AlertTriangle,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "صلاحية الإشعارات معطلة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "التنبيهات مفعّلة في الحساب ولكنها محجوبة من إعدادات الهاتف. اضغط هنا لتفعيلها.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            val isFallbackToken = state.currentToken?.startsWith("fcm_token_") == true
            if (state.isNotificationsEnabled && isFallbackToken) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.small),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            imageVector = LucideIcons.AlertTriangle,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "محدودية التنبيهات في هذا الجهاز",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "خدمات Google Play غير متوفرة على هاتفك. التنبيهات تعمل محلياً فقط.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Display error banner only if an explicit error occurs (green success message removed)
            if (state.isError && state.userMessage != null) {
                ProfileMessageBanner(
                    message = state.userMessage,
                    isError = true,
                    onDismiss = onDismissMessage
                )
            }
        }
    }
}

@Composable
private fun ThemeSettingsCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_settings_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDark) LucideIcons.Moon else LucideIcons.Sun,
                                contentDescription = stringResource(R.string.theme_settings_title),
                                tint = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.theme_dark_mode_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDark) {
                                stringResource(R.string.theme_status_dark)
                            } else {
                                stringResource(R.string.theme_status_light)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isDark,
                    onCheckedChange = { checked ->
                        onThemeModeChange(if (checked) ThemeMode.DARK else ThemeMode.LIGHT)
                    },
                    modifier = Modifier.testTag("theme_switch"),
                    thumbContent = {
                        Icon(
                            imageVector = if (isDark) LucideIcons.Moon else LucideIcons.Sun,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AppVersionFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "1.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag("app_version_text")
        )
    }
}

