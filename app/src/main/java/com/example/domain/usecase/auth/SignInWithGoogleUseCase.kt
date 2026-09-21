package com.example.domain.usecase.auth

import com.example.core.result.AppResult
import com.example.core.validation.AuthValidator
import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String, rawNonce: String? = null): AppResult<AuthUser> {
        val tokenValidation = AuthValidator.validateIdToken(idToken)
        if (tokenValidation is AppResult.Failure) return tokenValidation

        val sanitizedToken = (tokenValidation as AppResult.Success).data
        return authRepository.signInWithGoogle(sanitizedToken, rawNonce)
    }
}
