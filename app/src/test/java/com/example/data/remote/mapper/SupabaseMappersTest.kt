package com.example.data.remote.mapper

import com.example.data.local.ArchitectureEntity
import com.example.data.remote.dto.SupabaseRecordDto
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SupabaseMappersTest {

    @Test
    fun `SupabaseRecordMapper converts domain to dto and back`() {
        val domain = ArchitectureRecord(
            id = "rec-123",
            title = "Test Clean Architecture",
            timestamp = 1700000000L,
            syncStatus = SyncStatus.SYNCED,
            lastSyncedAt = 1700000500L
        )

        val dto = SupabaseRecordMapper.toDto(domain, userId = "usr-456")
        assertEquals("rec-123", dto.id)
        assertEquals("Test Clean Architecture", dto.title)
        assertEquals(1700000000L, dto.createdAt)
        assertEquals("usr-456", dto.userId)

        val mappedBack = SupabaseRecordMapper.toDomain(dto)
        assertEquals(domain.id, mappedBack.id)
        assertEquals(domain.title, mappedBack.title)
        assertEquals(domain.timestamp, mappedBack.timestamp)
        assertEquals(SyncStatus.SYNCED, mappedBack.syncStatus)
    }

    @Test
    fun `SupabaseRecordMapper converts entity to dto and entity from dto`() {
        val entity = ArchitectureEntity(
            id = "ent-1",
            title = "Entity Title",
            timestamp = 1600000000L,
            syncStatus = "SYNCED",
            lastSyncedAt = 1600000100L
        )

        val dto = SupabaseRecordMapper.toDto(entity)
        assertEquals(entity.id, dto.id)
        assertEquals(entity.title, dto.title)

        val entityFromDto = SupabaseRecordMapper.toEntity(dto)
        assertEquals(entity.id, entityFromDto.id)
        assertEquals(entity.title, entityFromDto.title)
        assertEquals("SYNCED", entityFromDto.syncStatus)
    }
}
