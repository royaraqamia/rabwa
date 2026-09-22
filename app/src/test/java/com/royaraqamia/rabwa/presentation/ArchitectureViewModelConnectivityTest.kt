package com.royaraqamia.rabwa.presentation

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.model.LayerHealth
import com.royaraqamia.rabwa.domain.model.NetworkStatus
import com.royaraqamia.rabwa.domain.model.SyncStatus
import com.royaraqamia.rabwa.domain.model.SyncSummary
import com.royaraqamia.rabwa.domain.network.ConnectivityMonitor
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository
import com.royaraqamia.rabwa.domain.usecase.AddArchitectureRecordUseCase
import com.royaraqamia.rabwa.domain.usecase.ClearRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.GetLayerHealthUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveNetworkStatusUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveRecordsUseCase
import com.royaraqamia.rabwa.domain.usecase.ObserveSyncSummaryUseCase
import com.royaraqamia.rabwa.domain.usecase.SyncOfflineRecordsUseCase
import com.royaraqamia.rabwa.testutil.TestCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArchitectureViewModelConnectivityTest {

    private lateinit var fakeRepository: FakeArchitectureRepository
    private lateinit var fakeConnectivityMonitor: FakeConnectivityMonitor
    private lateinit var dispatchers: TestCoroutineDispatchers
    private lateinit var viewModel: ArchitectureViewModel

    @Before
    fun setUp() {
        dispatchers = TestCoroutineDispatchers()
        fakeRepository = FakeArchitectureRepository()
        fakeConnectivityMonitor = FakeConnectivityMonitor()
        viewModel = ArchitectureViewModel(
            getLayerHealthUseCase = GetLayerHealthUseCase(fakeRepository),
            addArchitectureRecordUseCase = AddArchitectureRecordUseCase(fakeRepository),
            observeRecordsUseCase = ObserveRecordsUseCase(fakeRepository),
            clearRecordsUseCase = ClearRecordsUseCase(fakeRepository),
            observeNetworkStatusUseCase = ObserveNetworkStatusUseCase(fakeConnectivityMonitor),
            syncOfflineRecordsUseCase = SyncOfflineRecordsUseCase(fakeRepository),
            observeSyncSummaryUseCase = ObserveSyncSummaryUseCase(fakeRepository),
            dispatchers = dispatchers
        )
    }

    @Test
    fun `initial network status reflects monitor initial state`() {
        assertEquals(NetworkStatus.Available, viewModel.networkStatus.value)
        assertTrue(viewModel.networkStatus.value.isConnected)
        assertFalse(viewModel.isOffline.value)
        assertNull(viewModel.globalError.value)
    }

    @Test
    fun `when network becomes disconnected, system operates offline without fatal global error`() = runTest {
        fakeConnectivityMonitor.emitStatus(NetworkStatus.Unavailable)

        assertEquals(NetworkStatus.Unavailable, viewModel.networkStatus.value)
        assertFalse(viewModel.networkStatus.value.isConnected)
        assertTrue(viewModel.isOffline.value)

        // Offline-first invariant: lacking network connectivity is an operational state, NOT a blocking error
        assertNull(viewModel.globalError.value)
    }

    @Test
    fun `when network becomes lost, system smoothly transitions to offline mode`() = runTest {
        fakeConnectivityMonitor.emitStatus(NetworkStatus.Lost)

        assertEquals(NetworkStatus.Lost, viewModel.networkStatus.value)
        assertFalse(viewModel.networkStatus.value.isConnected)
        assertTrue(viewModel.isOffline.value)
        assertNull(viewModel.globalError.value)
    }

    @Test
    fun `when network reconnects after disconnection, auto-sync triggers and offline mode clears`() = runTest {
        // Disconnect
        fakeConnectivityMonitor.emitStatus(NetworkStatus.Unavailable)
        assertTrue(viewModel.isOffline.value)

        fakeRepository.setPendingSyncCount(3)

        // Reconnect
        fakeConnectivityMonitor.emitStatus(NetworkStatus.Available)
        assertEquals(NetworkStatus.Available, viewModel.networkStatus.value)
        assertTrue(viewModel.networkStatus.value.isConnected)
        assertFalse(viewModel.isOffline.value)

        // Verifies auto-sync occurred
        assertTrue(fakeRepository.syncCalled)
    }

    @Test
    fun `manual sync when offline displays graceful error feedback`() = runTest {
        fakeConnectivityMonitor.emitStatus(NetworkStatus.Unavailable)
        viewModel.syncPendingRecords(showFeedbackIfEmpty = true)

        val feedback = viewModel.actionFeedback.value
        assertTrue(feedback is AppResult.Failure)
    }

    @Test
    fun `clearAll triggers asynchronous database clear and resets isClearing flag`() = runTest {
        assertFalse(viewModel.isClearing.value)
        viewModel.clearAll()
        assertFalse(viewModel.isClearing.value)
        assertTrue(viewModel.actionFeedback.value is AppResult.Success)
    }

    @Test
    fun `submitRecord while offline records item with offline feedback`() = runTest {
        fakeConnectivityMonitor.emitStatus(NetworkStatus.Unavailable)
        viewModel.onTitleChanged("Offline note created locally")
        viewModel.submitRecord()

        assertFalse(viewModel.isSubmitting.value)
        val feedback = viewModel.actionFeedback.value
        assertTrue(feedback is AppResult.Success)
        val msg = (feedback as AppResult.Success).data
        assertTrue(msg.contains("محلياً"))
    }

    private class FakeConnectivityMonitor : ConnectivityMonitor {
        val statusFlow = MutableSharedFlow<NetworkStatus>(replay = 1)

        init {
            statusFlow.tryEmit(NetworkStatus.Available)
        }

        override val networkStatus: Flow<NetworkStatus> = statusFlow
        override var isCurrentlyConnected: Boolean = true

        suspend fun emitStatus(status: NetworkStatus) {
            isCurrentlyConnected = status.isConnected
            statusFlow.emit(status)
        }
    }

    private class FakeArchitectureRepository : ArchitectureRepository {
        private val recordsFlow = MutableStateFlow<List<ArchitectureRecord>>(emptyList())
        private val syncSummaryFlow = MutableStateFlow(SyncSummary())
        var syncCalled = false
            private set

        fun setPendingSyncCount(count: Int) {
            syncSummaryFlow.value = SyncSummary(pendingCount = count, syncedCount = 0)
        }

        override suspend fun getLayerHealth(): AppResult<List<LayerHealth>> {
            return AppResult.Success(emptyList())
        }

        override suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord> {
            val record = ArchitectureRecord(
                id = "1",
                title = sanitizedTitle,
                timestamp = 0L,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            return AppResult.Success(record)
        }

        override suspend fun clearRecords(): AppResult<Unit> = AppResult.Success(Unit)

        override fun observeRecords(): Flow<List<ArchitectureRecord>> = recordsFlow.asStateFlow()

        override fun observeSyncSummary(): Flow<SyncSummary> = syncSummaryFlow.asStateFlow()

        override suspend fun synchronizePendingRecords(): AppResult<Int> {
            syncCalled = true
            val count = syncSummaryFlow.value.pendingCount
            syncSummaryFlow.value = SyncSummary(pendingCount = 0, syncedCount = count)
            return AppResult.Success(count)
        }
    }
}
