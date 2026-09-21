package com.example.presentation.qr.state

import com.example.domain.model.qr.QrScanResult

/**
 * UI State for the QR Code Scanner interface.
 */
data class QrScannerUiState(
    val isScannerOpen: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val isProcessing: Boolean = false,
    val scanResult: QrScanResult? = null,
    val errorMessage: String? = null,
    val copiedFeedback: Boolean = false
)
