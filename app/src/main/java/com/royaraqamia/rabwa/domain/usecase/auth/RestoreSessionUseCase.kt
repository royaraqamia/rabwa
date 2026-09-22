package com.royaraqamia.rabwa.domain.usecase.auth

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.AuthRepository

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
