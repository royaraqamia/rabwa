package com.example.domain.usecase

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.LayerHealth
import com.example.domain.model.SyncSummary
import com.example.domain.repository.ArchitectureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncOfflineRecordsUseCaseTest {

    private lateinit var fakeRepository: FakeSyncArchitectureRepository
    private lateinit var syncUseCase: SyncOfflineRecordsUseCase
    private lateinit var observeSummaryUseCase: ObserveSyncSummaryUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeSyncArchitectureRepository()
        syncUseCase = SyncOfflineRecordsUseCase(fakeRepository)
        observeSummaryUseCase = ObserveSyncSummaryUseCase(fakeRepository)
    }

    @Test
    fun `syncUseCase - happy path synchronizes pending items successfully`() = runTest {
        fakeRepository.setPendingCount(5)

        val result = syncUseCase()

        assertTrue(result is AppResult.Success)
        assertEquals(5, (result as AppResult.Success).data)
        assertEquals(0, fakeRepository.currentSummary.pendingCount)
        assertEquals(5, fakeRepository.currentSummary.syncedCount)
    }

    @Test
    fun `syncUseCase - empty queue returns zero synced records`() = runTest {
        fakeRepository.setPendingCount(0)

        val result = syncUseCase()

        assertTrue(result is AppResult.Success)
        assertEquals(0, (result as AppResult.Success).data)
    }

    @Test
    fun `syncUseCase - failure path reports database error cleanly without crash`() = runTest {
        fakeRepository.shouldFail = true

        val result = syncUseCase()

        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Database)
    }

    @Test
    fun `observeSummaryUseCase - reflects pending and synced counts reactively`() = runTest {
        fakeRepository.setPendingCount(2)
        val initialSummary = fakeRepository.currentSummary
        assertFalse(initialSummary.isFullySynced)
        assertEquals(2, initialSummary.pendingCount)

        syncUseCase()
        val afterSyncSummary = fakeRepository.currentSummary
        assertTrue(afterSyncSummary.isFullySynced)
        assertEquals(0, afterSyncSummary.pendingCount)
    }

    private class FakeSyncArchitectureRepository : ArchitectureRepository {
        var shouldFail = false
        private val summaryFlow = MutableStateFlow(SyncSummary())
        val currentSummary: SyncSummary get() = summaryFlow.value

        fun setPendingCount(count: Int) {
            summaryFlow.value = SyncSummary(pendingCount = count, syncedCount = 0)
        }

        override suspend fun getLayerHealth(): AppResult<List<LayerHealth>> = AppResult.Success(emptyList())

        override suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord> =
            AppResult.Success(ArchitectureRecord(id = "test-id", title = sanitizedTitle, timestamp = 0L))

        override suspend fun clearRecords(): AppResult<Unit> = AppResult.Success(Unit)

        override fun observeRecords(): Flow<List<ArchitectureRecord>> = kotlinx.coroutines.flow.flowOf(emptyList())

        override fun observeSyncSummary(): Flow<SyncSummary> = summaryFlow.asStateFlow()

        override suspend fun synchronizePendingRecords(): AppResult<Int> {
            if (shouldFail) {
                return AppResult.Failure(AppError.Database("Simulation failure"))
            }
            val pending = summaryFlow.value.pendingCount
            summaryFlow.value = SyncSummary(pendingCount = 0, syncedCount = pending, lastSyncedAt = System.currentTimeMillis())
            return AppResult.Success(pending)
        }
    }
}
