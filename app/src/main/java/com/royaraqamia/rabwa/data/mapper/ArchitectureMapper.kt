package com.royaraqamia.rabwa.data.mapper

import com.royaraqamia.rabwa.data.local.ArchitectureEntity
import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.model.SyncStatus

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
