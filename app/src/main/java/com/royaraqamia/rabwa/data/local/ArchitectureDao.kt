package com.royaraqamia.rabwa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchitectureDao {
    @Query("SELECT * FROM architecture_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ArchitectureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ArchitectureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ArchitectureEntity>)

    @Query("SELECT * FROM architecture_records WHERE sync_status = 'PENDING_SYNC' ORDER BY timestamp ASC")
    suspend fun getPendingSyncRecords(): List<ArchitectureEntity>

    @Query("SELECT COUNT(*) FROM architecture_records WHERE sync_status = 'PENDING_SYNC'")
    fun observePendingSyncCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM architecture_records WHERE sync_status = 'SYNCED'")
    fun observeSyncedCount(): Flow<Int>

    @Query("UPDATE architecture_records SET sync_status = :status, last_synced_at = :syncedAt WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, status: String, syncedAt: Long)

    @Query("DELETE FROM architecture_records")
    suspend fun clearAll()
}
