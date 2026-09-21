package com.example.domain.usecase.auth

import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthUser? = authRepository.getCurrentUser()
}
