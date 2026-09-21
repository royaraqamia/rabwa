package com.example.domain.model.auth

data class UserSession(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAt: Long? = null,
    val user: AuthUser
)
