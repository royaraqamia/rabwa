package com.royaraqamia.rabwa.presentation.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royaraqamia.rabwa.core.dispatcher.CoroutineDispatchers
import com.royaraqamia.rabwa.core.dispatcher.DefaultDispatchers
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.usecase.qr.ProcessQrScanUseCase
import com.royaraqamia.rabwa.presentation.qr.state.QrScannerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel managing the QR Code Scanner interface, camera state, and scan results.
 */
class QrScannerViewModel(
    private val processQrScanUseCase: ProcessQrScanUseCase = ProcessQrScanUseCase(),
    private val dispatchers: CoroutineDispatchers = DefaultDispatchers()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrScannerUiState())
    val uiState: StateFlow<QrScannerUiState> = _uiState.asStateFlow()

    fun openScanner() {
        _uiState.update {
            it.copy(
                isScannerOpen = true,
                errorMessage = null,
                copiedFeedback = false
            )
        }
    }

    fun closeScanner() {
        _uiState.update {
            it.copy(
                isScannerOpen = false,
                isTorchEnabled = false,
                errorMessage = null,
                copiedFeedback = false
            )
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasCameraPermission = granted,
                errorMessage = if (!granted) "إذن الكاميرا مطلوب لمسح رمز QR" else null
            )
        }
    }

    fun onQrCodeDetected(rawContent: String) {
        // Prevent duplicate processing if already displaying a result
        if (_uiState.value.scanResult != null || _uiState.value.isProcessing) return

        _uiState.update { it.copy(isProcessing = true) }

        viewModelScope.launch(dispatchers.default) {
            when (val result = processQrScanUseCase(rawContent)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            scanResult = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchEnabled = !it.isTorchEnabled) }
    }

    fun dismissResult() {
        _uiState.update {
            it.copy(
                scanResult = null,
                isProcessing = false,
                copiedFeedback = false
            )
        }
    }

    fun setCopiedFeedback(copied: Boolean) {
        _uiState.update { it.copy(copiedFeedback = copied) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
