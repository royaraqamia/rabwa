package com.royaraqamia.rabwa.data.remote.mapper

import com.royaraqamia.rabwa.domain.model.auth.AuthProviderType
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.model.auth.UserSession
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession as SupabaseUserSession
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object AuthDtoMapper {

    fun toDomain(userInfo: UserInfo?): AuthUser? {
        if (userInfo == null) return null

        val email = userInfo.email
        val metadata = userInfo.userMetadata

        val fullName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("user_name")?.jsonPrimitive?.contentOrNull

        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull

        val providerStr = userInfo.appMetadata?.get("provider")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("provider")?.jsonPrimitive?.contentOrNull

        val provider = when (providerStr?.lowercase()) {
            "google" -> AuthProviderType.GOOGLE
            "email" -> AuthProviderType.EMAIL
            "anonymous" -> AuthProviderType.ANONYMOUS
            else -> if (email != null) AuthProviderType.EMAIL else AuthProviderType.UNKNOWN
        }

        val isConfirmed = userInfo.confirmedAt != null || userInfo.emailConfirmedAt != null

        return AuthUser(
            id = userInfo.id,
            email = email,
            displayName = fullName,
            avatarUrl = avatarUrl,
            isEmailConfirmed = isConfirmed,
            provider = provider,
            createdAt = runCatching { userInfo.createdAt?.toEpochMilliseconds() }.getOrNull()
        )
    }

    fun toDomainSession(session: SupabaseUserSession?): UserSession? {
        if (session == null) return null
        val domainUser = toDomain(session.user) ?: return null

        return UserSession(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAt = session.expiresAt?.toEpochMilliseconds(),
            user = domainUser
        )
    }
}
