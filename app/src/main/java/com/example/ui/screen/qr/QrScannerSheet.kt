package com.example.ui.screen.qr

import android.Manifest
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.example.R
import com.example.core.qr.QrCodeAnalyzer
import com.example.presentation.qr.QrScannerViewModel
import com.example.ui.component.qr.QrScanResultCard
import com.example.ui.component.qr.QrViewfinderOverlay
import com.example.ui.icon.AlertTriangle
import com.example.ui.icon.Flashlight
import com.example.ui.icon.LucideIcons
import com.example.ui.theme.spacing
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrScannerDialog(
    viewModel: QrScannerViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.onPermissionResult(cameraPermissionState.status.isGranted)
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.testTag("qr_scanner_dialog")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (uiState.hasCameraPermission) {
                // Camera Preview and Frame Analyzer
                var camera by remember { mutableStateOf<Camera?>(null) }
                var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                val analyzer = remember {
                    QrCodeAnalyzer { scannedText ->
                        viewModel.onQrCodeDetected(scannedText)
                    }
                }

                // Enable/disable analyzer depending on whether result is currently displayed
                LaunchedEffect(uiState.scanResult) {
                    analyzer.setScanningEnabled(uiState.scanResult == null)
                }

                // Handle Torch Toggle safely
                LaunchedEffect(uiState.isTorchEnabled, camera) {
                    val cam = camera
                    if (cam != null && cam.cameraInfo.hasFlashUnit()) {
                        try {
                            cam.cameraControl.enableTorch(uiState.isTorchEnabled)
                        } catch (_: Exception) {
                            // Suppress hardware torch errors
                        }
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    onDispose {
                        try {
                            cameraProvider?.unbindAll()
                        } catch (_: Exception) {
                            // Suppress cleanup exceptions
                        }
                        cameraExecutor.shutdown()
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val provider = cameraProviderFuture.get()
                                cameraProvider = provider
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also {
                                        it.setAnalyzer(cameraExecutor, analyzer)
                                    }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                provider.unbindAll()
                                camera = provider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (_: Exception) {
                                // Graceful fallback
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("qr_camera_preview")
                )

                // Viewfinder and Laser Line
                QrViewfinderOverlay(
                    isScanningActive = uiState.scanResult == null
                )
            } else {
                // Camera Permission Required Fallback UI
                CameraPermissionFallback(
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    },
                    showRationale = cameraPermissionState.status.shouldShowRationale,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Top Controls Bar (Header, Torch, Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.large)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flashlight / Torch Toggle
                if (uiState.hasCameraPermission) {
                    FilledIconButton(
                        onClick = { viewModel.toggleTorch() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (uiState.isTorchEnabled) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("qr_torch_toggle_button")
                    ) {
                        Icon(
                            imageVector = LucideIcons.Flashlight,
                            contentDescription = "تشغيل الفلاش",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Title
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "مسح رمز QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Close Button
                FilledIconButton(
                    onClick = {
                        viewModel.closeScanner()
                        onDismiss()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("qr_close_button")
                ) {
                    Text("✕", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom Result Display
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MaterialTheme.spacing.medium)
            ) {
                AnimatedVisibility(
                    visible = uiState.scanResult != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    uiState.scanResult?.let { result ->
                        QrScanResultCard(
                            result = result,
                            copiedFeedback = uiState.copiedFeedback,
                            onCopy = { viewModel.setCopiedFeedback(true) },
                            onRescan = { viewModel.dismissResult() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionFallback(
    onRequestPermission: () -> Unit,
    showRationale: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(32.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(24.dp)
            .testTag("qr_permission_fallback"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = LucideIcons.AlertTriangle,
            contentDescription = "إذن الكاميرا",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "مطلوب إذن الكاميرا",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (showRationale) {
                "التطبيق يحتاج الوصول إلى الكاميرا لمسح رموز QR. يرجى توفير الإذن لمتابعة العملية."
            } else {
                "يرجى منح إذن الوصول إلى الكاميرا لمسح رموز الاستجابة السريعة (QR Code) مباشرة."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRequestPermission,
                shape = CircleShape,
                modifier = Modifier.testTag("qr_permission_request_button")
            ) {
                Text("منح الإذن")
            }

            androidx.compose.material3.OutlinedButton(
                onClick = {
                    try {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", context.packageName, null)
                        ).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // Suppress intent failure
                    }
                },
                shape = CircleShape,
                modifier = Modifier.testTag("qr_app_settings_button")
            ) {
                Text("الإعدادات")
            }
        }
    }
}
