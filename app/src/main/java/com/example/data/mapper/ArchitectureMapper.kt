package com.example.data.mapper

import com.example.data.local.ArchitectureEntity
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.SyncStatus

fun ArchitectureEntity.toDomain(): ArchitectureRecord = ArchitectureRecord(
    id = id,
    title = title,
    timestamp = timestamp,
    syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.SYNCED),
    lastSyncedAt = lastSyncedAt
)

fun ArchitectureRecord.toEntity(): ArchitectureEntity = ArchitectureEntity(
    id = id,
    title = title,
    timestamp = timestamp,
    syncStatus = syncStatus.name,
    lastSyncedAt = lastSyncedAt
)
