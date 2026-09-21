package com.example.domain.usecase

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.LayerHealth
import com.example.domain.repository.ArchitectureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class AddArchitectureRecordUseCaseTest {

    private lateinit var fakeRepository: FakeArchitectureRepository
    private lateinit var useCase: AddArchitectureRecordUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeArchitectureRepository()
        useCase = AddArchitectureRecordUseCase(fakeRepository)
    }

    @Test
    fun `invoke - happy path adds record when title is valid`() = runTest {
        val result = useCase("Order Processing Gateway")
        assertTrue(result is AppResult.Success)
        val record = (result as AppResult.Success).data
        assertEquals("Order Processing Gateway", record.title)
        assertEquals(1, fakeRepository.records.size)
    }

    @Test
    fun `invoke - failure path rejects empty title without calling repository`() = runTest {
        val result = useCase("   ")
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Validation)
        assertEquals(0, fakeRepository.records.size)
    }

    @Test
    fun `invoke - failure path rejects title shorter than 2 chars`() = runTest {
        val result = useCase("X")
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Validation)
        assertEquals(0, fakeRepository.records.size)
    }

    @Test
    fun `invoke - failure path rejects title exceeding 60 chars`() = runTest {
        val longTitle = "A".repeat(61)
        val result = useCase(longTitle)
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Validation)
        assertEquals(0, fakeRepository.records.size)
    }

    private class FakeArchitectureRepository : ArchitectureRepository {
        val records = mutableListOf<ArchitectureRecord>()
        private val recordsFlow = MutableStateFlow<List<ArchitectureRecord>>(emptyList())

        override suspend fun getLayerHealth(): AppResult<List<LayerHealth>> {
            return AppResult.Success(emptyList())
        }

        override suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord> {
            val record = ArchitectureRecord(
                id = UUID.randomUUID().toString(),
                title = sanitizedTitle,
                timestamp = System.currentTimeMillis()
            )
            records.add(record)
            recordsFlow.value = records.toList()
            return AppResult.Success(record)
        }

        override suspend fun clearRecords(): AppResult<Unit> {
            records.clear()
            recordsFlow.value = emptyList()
            return AppResult.Success(Unit)
        }

        override fun observeRecords(): Flow<List<ArchitectureRecord>> {
            return recordsFlow.asStateFlow()
        }

        override fun observeSyncSummary(): Flow<com.example.domain.model.SyncSummary> {
            return kotlinx.coroutines.flow.flowOf(com.example.domain.model.SyncSummary())
        }

        override suspend fun synchronizePendingRecords(): AppResult<Int> {
            return AppResult.Success(0)
        }
    }
}
