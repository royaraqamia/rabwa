package com.example.domain.usecase.auth

import com.example.core.result.AppResult
import com.example.core.validation.AuthValidator
import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.AuthRepository

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
