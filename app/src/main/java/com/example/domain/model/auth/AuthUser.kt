package com.example.domain.model.auth

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isEmailConfirmed: Boolean = false,
    val provider: AuthProviderType = AuthProviderType.UNKNOWN,
    val createdAt: Long? = null
)
