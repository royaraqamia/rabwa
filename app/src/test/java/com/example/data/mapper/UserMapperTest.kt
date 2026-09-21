package com.example.data.mapper

import com.example.data.local.UserEntity
import com.example.domain.model.auth.AuthProviderType
import com.example.domain.model.auth.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserMapperTest {

    @Test
    fun `toEntity correctly converts AuthUser to UserEntity`() {
        val user = AuthUser(
            id = "user-123",
            email = "architect@enterprise.com",
            displayName = "Senior Architect",
            avatarUrl = "https://avatar.url/photo.jpg",
            isEmailConfirmed = true,
            provider = AuthProviderType.EMAIL,
            createdAt = 1700000000L
        )

        val timestamp = 1710000000L
        val entity = user.toEntity(lastCachedAt = timestamp)

        assertEquals("user-123", entity.id)
        assertEquals("architect@enterprise.com", entity.email)
        assertEquals("Senior Architect", entity.displayName)
        assertEquals("https://avatar.url/photo.jpg", entity.avatarUrl)
        assertTrue(entity.isEmailConfirmed)
        assertEquals("EMAIL", entity.provider)
        assertEquals(1700000000L, entity.createdAt)
        assertEquals(timestamp, entity.lastCachedAt)
    }

    @Test
    fun `toDomain correctly converts UserEntity to AuthUser`() {
        val entity = UserEntity(
            id = "user-456",
            email = "google@enterprise.com",
            displayName = "Google User",
            avatarUrl = null,
            isEmailConfirmed = true,
            provider = "GOOGLE",
            createdAt = 1690000000L,
            lastCachedAt = 1710000000L
        )

        val domain = entity.toDomain()

        assertEquals("user-456", domain.id)
        assertEquals("google@enterprise.com", domain.email)
        assertEquals("Google User", domain.displayName)
        assertEquals(null, domain.avatarUrl)
        assertTrue(domain.isEmailConfirmed)
        assertEquals(AuthProviderType.GOOGLE, domain.provider)
        assertEquals(1690000000L, domain.createdAt)
    }

    @Test
    fun `toDomain falls back to UNKNOWN provider for invalid string`() {
        val entity = UserEntity(
            id = "user-789",
            email = "unknown@enterprise.com",
            provider = "INVALID_PROVIDER_STRING"
        )

        val domain = entity.toDomain()

        assertEquals(AuthProviderType.UNKNOWN, domain.provider)
    }
}
