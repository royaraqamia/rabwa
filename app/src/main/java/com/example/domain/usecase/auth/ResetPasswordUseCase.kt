package com.example.domain.usecase.auth

import com.example.core.result.AppResult
import com.example.core.validation.AuthValidator
import com.example.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): AppResult<Unit> {
        val emailValidation = AuthValidator.validateEmail(email)
        if (emailValidation is AppResult.Failure) return emailValidation

        val sanitizedEmail = (emailValidation as AppResult.Success).data
        return authRepository.resetPassword(sanitizedEmail)
    }
}
