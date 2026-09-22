package com.royaraqamia.rabwa.data.mapper

import com.royaraqamia.rabwa.data.local.UserEntity
import com.royaraqamia.rabwa.domain.model.auth.AuthProviderType
import com.royaraqamia.rabwa.domain.model.auth.AuthUser

fun AuthUser.toEntity(lastCachedAt: Long = System.currentTimeMillis()): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        isEmailConfirmed = isEmailConfirmed,
        provider = provider.name,
        createdAt = createdAt,
        lastCachedAt = lastCachedAt
    )
}

fun UserEntity.toDomain(): AuthUser {
    val parsedProvider = try {
        AuthProviderType.valueOf(provider)
    } catch (_: Exception) {
        AuthProviderType.UNKNOWN
    }

    return AuthUser(
        id = id,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        isEmailConfirmed = isEmailConfirmed,
        provider = parsedProvider,
        createdAt = createdAt
    )
}
