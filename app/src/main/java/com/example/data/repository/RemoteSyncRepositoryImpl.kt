package com.example.data.repository

import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.dispatcher.DefaultDispatchers
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.data.local.ArchitectureDao
import com.example.data.remote.datasource.DatabaseRemoteDataSource
import com.example.data.remote.datasource.SupabaseDatabaseRemoteDataSource
import com.example.data.remote.mapper.SupabaseRecordMapper
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.sync.RemoteSyncResult
import com.example.domain.repository.RemoteSyncRepository
import kotlinx.coroutines.withContext

class RemoteSyncRepositoryImpl(
    private val dao: ArchitectureDao,
    private val remoteDataSource: DatabaseRemoteDataSource = SupabaseDatabaseRemoteDataSource(),
    private val dispatchers: CoroutineDispatchers = DefaultDispatchers()
) : RemoteSyncRepository {

    override suspend fun fetchRemoteRecords(): AppResult<List<ArchitectureRecord>> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.fetchRecords()) {
            is AppResult.Success -> {
                val records = result.data.map { SupabaseRecordMapper.toDomain(it) }
                // Update local cache
                val entities = result.data.map { SupabaseRecordMapper.toEntity(it) }
                dao.insertAll(entities)
                AppResult.Success(records)
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun pushRecord(record: ArchitectureRecord): AppResult<ArchitectureRecord> = withContext(dispatchers.io) {
        val dto = SupabaseRecordMapper.toDto(record)
        when (val result = remoteDataSource.upsertRecord(dto)) {
            is AppResult.Success -> {
                val now = System.currentTimeMillis()
                dao.updateSyncStatus(listOf(record.id), "SYNCED", now)
                AppResult.Success(record.copy(lastSyncedAt = now))
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun deleteRemoteRecord(id: String): AppResult<Unit> = withContext(dispatchers.io) {
        remoteDataSource.deleteRecord(id)
    }

    override suspend fun syncPendingRecords(): AppResult<RemoteSyncResult> = withContext(dispatchers.io) {
        val pendingEntities = dao.getPendingSyncRecords()
        if (pendingEntities.isEmpty()) {
            return@withContext AppResult.Success(RemoteSyncResult(0, 0, 0))
        }

        val dtos = pendingEntities.map { SupabaseRecordMapper.toDto(it) }
        when (val result = remoteDataSource.bulkUpsert(dtos)) {
            is AppResult.Success -> {
                val now = System.currentTimeMillis()
                val ids = pendingEntities.map { it.id }
                dao.updateSyncStatus(ids, "SYNCED", now)
                AppResult.Success(
                    RemoteSyncResult(
                        totalProcessed = pendingEntities.size,
                        successCount = pendingEntities.size,
                        failedCount = 0
                    )
                )
            }
            is AppResult.Failure -> {
                AppResult.Failure(result.error)
            }
        }
    }
}
