package com.example.data.repository

import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.result.AppResult
import com.example.data.local.ArchitectureDao
import com.example.data.local.ArchitectureEntity
import com.example.data.remote.datasource.DatabaseRemoteDataSource
import com.example.data.remote.dto.SupabaseRecordDto
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.SyncStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteSyncRepositoryImplTest {

    private class TestDispatchers : CoroutineDispatchers {
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
    }

    private class FakeArchitectureDao : ArchitectureDao {
        val records = mutableListOf<ArchitectureEntity>()

        override fun observeAll(): Flow<List<ArchitectureEntity>> = flowOf(records)

        override suspend fun insert(entity: ArchitectureEntity) {
            records.removeAll { it.id == entity.id }
            records.add(entity)
        }

        override suspend fun insertAll(entities: List<ArchitectureEntity>) {
            for (e in entities) {
                insert(e)
            }
        }

        override suspend fun getPendingSyncRecords(): List<ArchitectureEntity> {
            return records.filter { it.syncStatus == "PENDING_SYNC" }
        }

        override fun observePendingSyncCount(): Flow<Int> =
            flowOf(records.count { it.syncStatus == "PENDING_SYNC" })

        override fun observeSyncedCount(): Flow<Int> =
            flowOf(records.count { it.syncStatus == "SYNCED" })

        override suspend fun updateSyncStatus(ids: List<String>, status: String, syncedAt: Long) {
            for (i in records.indices) {
                if (records[i].id in ids) {
                    records[i] = records[i].copy(syncStatus = status, lastSyncedAt = syncedAt)
                }
            }
        }

        override suspend fun clearAll() {
            records.clear()
        }
    }

    private class FakeDatabaseRemoteDataSource : DatabaseRemoteDataSource {
        val remoteRecords = mutableListOf<SupabaseRecordDto>()
        var shouldFail: Boolean = false

        override suspend fun fetchRecords(tableName: String): AppResult<List<SupabaseRecordDto>> {
            if (shouldFail) return AppResult.Failure(com.example.core.result.AppError.RemoteDatabase())
            return AppResult.Success(remoteRecords)
        }

        override suspend fun upsertRecord(record: SupabaseRecordDto, tableName: String): AppResult<SupabaseRecordDto> {
            remoteRecords.removeAll { it.id == record.id }
            remoteRecords.add(record)
            return AppResult.Success(record)
        }

        override suspend fun bulkUpsert(records: List<SupabaseRecordDto>, tableName: String): AppResult<Unit> {
            for (r in records) {
                upsertRecord(r, tableName)
            }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteRecord(id: String, tableName: String): AppResult<Unit> {
            remoteRecords.removeAll { it.id == id }
            return AppResult.Success(Unit)
        }
    }

    @Test
    fun `syncPendingRecords bulk-pushes pending records and updates local sync status`() = runBlocking {
        val dao = FakeArchitectureDao()
        val remoteDataSource = FakeDatabaseRemoteDataSource()
        val repository = RemoteSyncRepositoryImpl(
            dao = dao,
            remoteDataSource = remoteDataSource,
            dispatchers = TestDispatchers()
        )

        // Seed pending entities
        dao.insert(ArchitectureEntity("1", "Offline Task 1", 1000L, "PENDING_SYNC"))
        dao.insert(ArchitectureEntity("2", "Offline Task 2", 2000L, "PENDING_SYNC"))

        val result = repository.syncPendingRecords()
        assertTrue(result is AppResult.Success)
        val summary = (result as AppResult.Success).data
        assertEquals(2, summary.totalProcessed)
        assertEquals(2, summary.successCount)
        assertEquals(0, summary.failedCount)

        // Verify remote received both records
        assertEquals(2, remoteDataSource.remoteRecords.size)

        // Verify local state changed to SYNCED
        val remainingPending = dao.getPendingSyncRecords()
        assertTrue(remainingPending.isEmpty())
    }

    @Test
    fun `fetchRemoteRecords updates local cache with remote records`() = runBlocking {
        val dao = FakeArchitectureDao()
        val remoteDataSource = FakeDatabaseRemoteDataSource()
        val repository = RemoteSyncRepositoryImpl(
            dao = dao,
            remoteDataSource = remoteDataSource,
            dispatchers = TestDispatchers()
        )

        remoteDataSource.remoteRecords.add(
            SupabaseRecordDto(id = "rem-1", title = "Remote Cloud Record", createdAt = 5000L)
        )

        val result = repository.fetchRemoteRecords()
        assertTrue(result is AppResult.Success)
        val records = (result as AppResult.Success).data
        assertEquals(1, records.size)
        assertEquals("Remote Cloud Record", records[0].title)

        // Check local cache
        assertEquals(1, dao.records.size)
        assertEquals("rem-1", dao.records[0].id)
    }
}
