package com.royaraqamia.rabwa.data.remote.mapper

import com.royaraqamia.rabwa.data.local.ArchitectureEntity
import com.royaraqamia.rabwa.data.remote.dto.SupabaseRecordDto
import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.model.SyncStatus

object SupabaseRecordMapper {

    fun toDto(record: ArchitectureRecord, userId: String? = null): SupabaseRecordDto {
        return SupabaseRecordDto(
            id = record.id,
            title = record.title,
            createdAt = record.timestamp,
            userId = userId,
            updatedAt = record.lastSyncedAt ?: System.currentTimeMillis()
        )
    }

    fun toDto(entity: ArchitectureEntity, userId: String? = null): SupabaseRecordDto {
        return SupabaseRecordDto(
            id = entity.id,
            title = entity.title,
            createdAt = entity.timestamp,
            userId = userId,
            updatedAt = entity.lastSyncedAt ?: System.currentTimeMillis()
        )
    }

    fun toDomain(dto: SupabaseRecordDto): ArchitectureRecord {
        return ArchitectureRecord(
            id = dto.id,
            title = dto.title,
            timestamp = dto.createdAt,
            syncStatus = SyncStatus.SYNCED,
            lastSyncedAt = dto.updatedAt ?: dto.createdAt
        )
    }

    fun toEntity(dto: SupabaseRecordDto): ArchitectureEntity {
        return ArchitectureEntity(
            id = dto.id,
            title = dto.title,
            timestamp = dto.createdAt,
            syncStatus = SyncStatus.SYNCED.name,
            lastSyncedAt = dto.updatedAt ?: dto.createdAt
        )
    }
}
