package com.royaraqamia.rabwa.domain.usecase.auth

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.core.validation.AuthValidator
import com.royaraqamia.rabwa.domain.repository.AuthRepository

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
