package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "architecture_records")
data class ArchitectureEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val timestamp: Long,
    @ColumnInfo(name = "sync_status", defaultValue = "SYNCED")
    val syncStatus: String = "SYNCED",
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
)
