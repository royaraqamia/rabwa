package com.example.data.remote.mapper

import com.example.data.local.ArchitectureEntity
import com.example.data.remote.dto.SupabaseRecordDto
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.SyncStatus

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
