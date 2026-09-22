package com.royaraqamia.rabwa.presentation.qr

import com.royaraqamia.rabwa.domain.model.qr.QrContentType
import com.royaraqamia.rabwa.domain.usecase.qr.ProcessQrScanUseCase
import com.royaraqamia.rabwa.testutil.TestCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QrScannerViewModelTest {

    private lateinit var testDispatchers: TestCoroutineDispatchers
    private lateinit var processQrScanUseCase: ProcessQrScanUseCase
    private lateinit var viewModel: QrScannerViewModel

    @Before
    fun setUp() {
        testDispatchers = TestCoroutineDispatchers()
        processQrScanUseCase = ProcessQrScanUseCase()
        viewModel = QrScannerViewModel(
            processQrScanUseCase = processQrScanUseCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `initial state is closed and idle`() {
        val state = viewModel.uiState.value
        assertFalse(state.isScannerOpen)
        assertFalse(state.hasCameraPermission)
        assertFalse(state.isTorchEnabled)
        assertFalse(state.isProcessing)
        assertNull(state.scanResult)
        assertNull(state.errorMessage)
    }

    @Test
    fun `openScanner sets isScannerOpen to true`() {
        viewModel.openScanner()
        assertTrue(viewModel.uiState.value.isScannerOpen)
    }

    @Test
    fun `closeScanner resets scanner state`() {
        viewModel.openScanner()
        viewModel.toggleTorch()
        assertTrue(viewModel.uiState.value.isTorchEnabled)

        viewModel.closeScanner()
        assertFalse(viewModel.uiState.value.isScannerOpen)
        assertFalse(viewModel.uiState.value.isTorchEnabled)
    }

    @Test
    fun `onPermissionResult updates permission flag correctly`() {
        viewModel.onPermissionResult(true)
        assertTrue(viewModel.uiState.value.hasCameraPermission)
        assertNull(viewModel.uiState.value.errorMessage)

        viewModel.onPermissionResult(false)
        assertFalse(viewModel.uiState.value.hasCameraPermission)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onQrCodeDetected successfully parses QR payload and updates scanResult`() = runTest {
        viewModel.openScanner()
        viewModel.onQrCodeDetected("https://example.com")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.scanResult)
        assertEquals("https://example.com", state.scanResult?.sanitizedContent)
        assertEquals(QrContentType.URL, state.scanResult?.contentType)
    }

    @Test
    fun `dismissResult clears current scan result for continuous scanning`() = runTest {
        viewModel.onQrCodeDetected("Hello World")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.scanResult)

        viewModel.dismissResult()
        assertNull(viewModel.uiState.value.scanResult)
        assertFalse(viewModel.uiState.value.isProcessing)
    }

    @Test
    fun `toggleTorch flips torch state`() {
        assertFalse(viewModel.uiState.value.isTorchEnabled)
        viewModel.toggleTorch()
        assertTrue(viewModel.uiState.value.isTorchEnabled)
        viewModel.toggleTorch()
        assertFalse(viewModel.uiState.value.isTorchEnabled)
    }

    @Test
    fun `setCopiedFeedback updates state flag`() {
        viewModel.setCopiedFeedback(true)
        assertTrue(viewModel.uiState.value.copiedFeedback)
        viewModel.setCopiedFeedback(false)
        assertFalse(viewModel.uiState.value.copiedFeedback)
    }
}
