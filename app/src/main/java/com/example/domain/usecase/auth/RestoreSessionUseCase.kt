package com.example.domain.usecase.auth

import com.example.core.result.AppResult
import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.AuthRepository

/**
 * Enterprise use case for restoring an authenticated user session from secure local storage
 * across app restarts.
 */
class RestoreSessionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<AuthUser?> {
        return authRepository.restoreSession()
    }
}
