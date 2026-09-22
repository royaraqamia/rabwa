package com.royaraqamia.rabwa.domain.usecase.auth

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.core.validation.AuthValidator
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.AuthRepository

class SignInWithEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<AuthUser> {
        val emailValidation = AuthValidator.validateEmail(email)
        if (emailValidation is AppResult.Failure) return emailValidation

        val passwordValidation = AuthValidator.validatePassword(password)
        if (passwordValidation is AppResult.Failure) return passwordValidation

        val sanitizedEmail = (emailValidation as AppResult.Success).data
        val sanitizedPassword = (passwordValidation as AppResult.Success).data

        return authRepository.signInWithEmail(sanitizedEmail, sanitizedPassword)
    }
}
