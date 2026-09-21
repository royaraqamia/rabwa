package com.example.domain.repository

import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.sync.RemoteSyncResult

/**
 * Enterprise contract for remote Supabase table synchronization.
 */
interface RemoteSyncRepository {
    suspend fun fetchRemoteRecords(): AppResult<List<ArchitectureRecord>>
    suspend fun pushRecord(record: ArchitectureRecord): AppResult<ArchitectureRecord>
    suspend fun deleteRemoteRecord(id: String): AppResult<Unit>
    suspend fun syncPendingRecords(): AppResult<RemoteSyncResult>
}
