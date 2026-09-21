package com.example.domain.usecase.auth

import com.example.core.result.AppResult
import com.example.domain.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> = authRepository.signOut()
}
