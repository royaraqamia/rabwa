package com.example.domain.repository

import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.LayerHealth
import com.example.domain.model.SyncSummary
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level contract for system architecture diagnostics and state.
 * Implemented in the Data layer following the Offline-First Single Source of Truth pattern.
 */
interface ArchitectureRepository {
    suspend fun getLayerHealth(): AppResult<List<LayerHealth>>
    suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord>
    suspend fun clearRecords(): AppResult<Unit>
    fun observeRecords(): Flow<List<ArchitectureRecord>>
    fun observeSyncSummary(): Flow<SyncSummary>
    suspend fun synchronizePendingRecords(): AppResult<Int>
}
