package com.royaraqamia.rabwa.domain.repository

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.model.sync.RemoteSyncResult

/**
 * Enterprise contract for remote Supabase table synchronization.
 */
interface RemoteSyncRepository {
    suspend fun fetchRemoteRecords(): AppResult<List<ArchitectureRecord>>
    suspend fun pushRecord(record: ArchitectureRecord): AppResult<ArchitectureRecord>
    suspend fun deleteRemoteRecord(id: String): AppResult<Unit>
    suspend fun syncPendingRecords(): AppResult<RemoteSyncResult>
}
