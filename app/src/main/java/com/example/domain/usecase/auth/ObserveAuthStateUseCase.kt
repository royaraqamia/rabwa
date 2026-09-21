package com.example.domain.usecase.auth

import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> = authRepository.observeAuthState()
}
