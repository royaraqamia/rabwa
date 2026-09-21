package com.example.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.core.dispatcher.DefaultDispatchers
import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.LayerHealth
import com.example.domain.model.NetworkStatus
import com.example.domain.model.SyncSummary
import com.example.domain.model.qr.QrContentType
import com.example.domain.model.qr.QrScanResult
import com.example.domain.network.ConnectivityMonitor
import com.example.domain.repository.ArchitectureRepository
import com.example.domain.usecase.AddArchitectureRecordUseCase
import com.example.domain.usecase.ClearRecordsUseCase
import com.example.domain.usecase.GetLayerHealthUseCase
import com.example.domain.usecase.ObserveNetworkStatusUseCase
import com.example.domain.usecase.ObserveRecordsUseCase
import com.example.domain.usecase.ObserveSyncSummaryUseCase
import com.example.domain.usecase.SyncOfflineRecordsUseCase
import com.example.domain.usecase.qr.ProcessQrScanUseCase
import com.example.presentation.ArchitectureViewModel
import com.example.presentation.qr.QrScannerViewModel
import com.example.ui.component.qr.QrScanResultCard
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ArchitectureDashboardScreenQrTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeArchitectureRepository : ArchitectureRepository {
        private val recordsFlow = MutableStateFlow<List<ArchitectureRecord>>(emptyList())
        private val syncFlow = MutableStateFlow(SyncSummary(0, 0, 0))

        override suspend fun getLayerHealth(): AppResult<List<LayerHealth>> = AppResult.Success(emptyList())
        override suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord> =
            AppResult.Success(ArchitectureRecord("1", sanitizedTitle, System.currentTimeMillis()))
        override suspend fun clearRecords(): AppResult<Unit> = AppResult.Success(Unit)
        override fun observeRecords(): Flow<List<ArchitectureRecord>> = recordsFlow.asStateFlow()
        override fun observeSyncSummary(): Flow<SyncSummary> = syncFlow.asStateFlow()
        override suspend fun synchronizePendingRecords(): AppResult<Int> = AppResult.Success(0)
    }

    private class FakeConnectivityMonitor : ConnectivityMonitor {
        private val statusFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)
        override val networkStatus: Flow<NetworkStatus> = statusFlow.asStateFlow()
        override val isCurrentlyConnected: Boolean = true
    }

    @Test
    fun `top app bar contains QR code entry icon and clicking it opens scanner`() {
        val archRepo = FakeArchitectureRepository()
        val connectivity = FakeConnectivityMonitor()
        val dispatchers = DefaultDispatchers()

        val archViewModel = ArchitectureViewModel(
            getLayerHealthUseCase = GetLayerHealthUseCase(archRepo),
            addArchitectureRecordUseCase = AddArchitectureRecordUseCase(archRepo),
            observeRecordsUseCase = ObserveRecordsUseCase(archRepo),
            clearRecordsUseCase = ClearRecordsUseCase(archRepo),
            observeNetworkStatusUseCase = ObserveNetworkStatusUseCase(connectivity),
            syncOfflineRecordsUseCase = SyncOfflineRecordsUseCase(archRepo),
            observeSyncSummaryUseCase = ObserveSyncSummaryUseCase(archRepo),
            dispatchers = dispatchers
        )

        val qrScannerViewModel = QrScannerViewModel(
            processQrScanUseCase = ProcessQrScanUseCase(),
            dispatchers = dispatchers
        )

        composeTestRule.setContent {
            MaterialTheme {
                ArchitectureDashboardScreen(
                    viewModel = archViewModel,
                    qrScannerViewModel = qrScannerViewModel,
                    themeMode = ThemeMode.SYSTEM
                )
            }
        }

        // Verify QR Scan Button exists on TopAppBar
        composeTestRule.onNodeWithTag("app_bar_qr_scan_button").assertIsDisplayed()

        // Click QR Scan Button
        composeTestRule.onNodeWithTag("app_bar_qr_scan_button").performClick()
        composeTestRule.waitForIdle()

        // Verify scanner opened in viewModel
        assertTrue(qrScannerViewModel.uiState.value.isScannerOpen)
    }

    @Test
    fun `qr result card renders properly with action buttons`() {
        val sampleResult = QrScanResult(
            rawContent = "https://example.com/item",
            sanitizedContent = "https://example.com/item",
            contentType = QrContentType.URL,
            title = "رابط ويب (URL)"
        )

        var copyClicked = false
        var rescanClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                QrScanResultCard(
                    result = sampleResult,
                    copiedFeedback = false,
                    onCopy = { copyClicked = true },
                    onRescan = { rescanClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("qr_result_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_copy_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_open_url_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_rescan_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("qr_copy_button").performClick()
        assertTrue(copyClicked)

        composeTestRule.onNodeWithTag("qr_rescan_button").performClick()
        assertTrue(rescanClicked)
    }
}
