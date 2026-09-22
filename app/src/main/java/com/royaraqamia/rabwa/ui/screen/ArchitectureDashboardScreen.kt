package com.royaraqamia.rabwa.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.royaraqamia.rabwa.ui.icon.AlertTriangle
import com.royaraqamia.rabwa.ui.icon.CheckCircle2
import com.royaraqamia.rabwa.ui.icon.LucideIcons
import com.royaraqamia.rabwa.ui.icon.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royaraqamia.rabwa.R
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.presentation.ArchitectureViewModel
import com.royaraqamia.rabwa.presentation.error.ErrorMessageResolver
import com.royaraqamia.rabwa.presentation.qr.QrScannerViewModel
import com.royaraqamia.rabwa.ui.component.GlobalErrorBanner
import com.royaraqamia.rabwa.ui.component.OfflineStatusBanner
import com.royaraqamia.rabwa.ui.icon.QrCode
import com.royaraqamia.rabwa.ui.screen.qr.QrScannerDialog
import com.royaraqamia.rabwa.ui.theme.ThemeMode
import com.royaraqamia.rabwa.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureDashboardScreen(
    viewModel: ArchitectureViewModel,
    qrScannerViewModel: QrScannerViewModel = viewModel(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val actionFeedback by viewModel.actionFeedback.collectAsStateWithLifecycle()
    val globalError by viewModel.globalError.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val syncSummary by viewModel.syncSummary.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val qrScannerUiState by qrScannerViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("architecture_dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_rabwah_logo),
                            contentDescription = "شعار رَبْوَة",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("app_bar_logo")
                        )
                        Text(
                            text = "رَبْوَة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // QR Code Scanner Entry Icon
                    IconButton(
                        onClick = { qrScannerViewModel.openScanner() },
                        modifier = Modifier.testTag("app_bar_qr_scan_button")
                    ) {
                        Icon(
                            imageVector = LucideIcons.QrCode,
                            contentDescription = stringResource(R.string.qr_scanner_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Real-time network status indicator (only show when offline)
                    if (!networkStatus.isConnected) {
                        Icon(
                            imageVector = LucideIcons.WifiOff,
                            contentDescription = stringResource(R.string.network_status_disconnected),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(horizontal = MaterialTheme.spacing.small)
                                .testTag("network_status_indicator")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                // Offline-First Status & Sync Banner
                item {
                    OfflineStatusBanner(
                        isOffline = isOffline,
                        syncSummary = syncSummary,
                        isSyncing = isSyncing,
                        onSyncNow = { viewModel.syncPendingRecords(showFeedbackIfEmpty = true) }
                    )
                }

                // Global Error Banner (Zero-leakage, localized, retryable)
                item {
                    GlobalErrorBanner(
                        error = globalError,
                        onDismiss = { viewModel.dismissGlobalError() },
                        onRetry = {
                            viewModel.dismissGlobalError()
                            viewModel.loadLayerHealth()
                        }
                    )
                }

                // Action Feedback Notification
                item {
                    AnimatedVisibility(
                        visible = actionFeedback != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        actionFeedback?.let { feedback ->
                            FeedbackBanner(
                                feedback = feedback,
                                onDismiss = { viewModel.dismissFeedback() }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
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
private fun FeedbackBanner(
    feedback: AppResult<String>,
    onDismiss: () -> Unit
) {
    val isSuccess = feedback is AppResult.Success
    val bgColor = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val textColor = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    val icon = if (isSuccess) LucideIcons.CheckCircle2 else LucideIcons.AlertTriangle
    val message = when (feedback) {
        is AppResult.Success -> feedback.data
        is AppResult.Failure -> {
            val userFacing = ErrorMessageResolver.resolve(feedback.error)
            val context = androidx.compose.ui.platform.LocalContext.current
            try {
                context.getString(userFacing.messageResId)
            } catch (e: Exception) {
                userFacing.fallbackMessage
            }
        }
    }

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
                contentDescription = if (isSuccess) "نجاح" else "تنبيه",
                tint = textColor,
                modifier = Modifier.size(22.dp)
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

