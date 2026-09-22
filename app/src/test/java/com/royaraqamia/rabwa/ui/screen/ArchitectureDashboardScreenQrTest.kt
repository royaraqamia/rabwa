package com.royaraqamia.rabwa.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.royaraqamia.rabwa.core.dispatcher.DefaultDispatchers
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.model.LayerHealth
import com.royaraqamia.rabwa.domain.model.NetworkStatus
import com.royaraqamia.rabwa.domain.model.SyncSummary
import com.royaraqamia.rabwa.domain.model.qr.QrContentType
import com.royaraqamia.rabwa.domain.model.qr.QrScanResult
import com.royaraqamia.rabwa.domain.network.ConnectivityMonitor
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository
import com.royaraqamia.rabwa.domain.usecase.AddArchitectureRecordUseCase
import com.royaraqamia.rabwa.domain.usecase.ClearRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.GetLayerHealthUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveNetworkStatusUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveSyncSummaryUseCase
import com.royaraqamia.rabwa.domain.usecase.SyncOfflineRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.qr.ProcessQrScanUseCase
import com.royaraqamia.rabwa.presentation.ArchitectureViewModel
import com.royaraqamia.rabwa.presentation.qr.QrScannerViewModel
import com.royaraqamia.rabwa.ui.component.qr.QrScanResultCard
import com.royaraqamia.rabwa.ui.theme.ThemeMode
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
