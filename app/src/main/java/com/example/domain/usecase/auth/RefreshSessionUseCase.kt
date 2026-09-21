package com.example.domain.usecase.auth

import com.example.core.result.AppResult
import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.AuthRepository

/**
 * Enterprise use case for explicitly refreshing Supabase Auth tokens.
 */
class RefreshSessionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<AuthUser> {
        return authRepository.refreshSession()
    }
}
