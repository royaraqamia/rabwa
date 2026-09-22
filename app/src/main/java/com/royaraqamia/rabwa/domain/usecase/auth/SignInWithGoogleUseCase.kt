package com.royaraqamia.rabwa.domain.usecase.auth

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.core.validation.AuthValidator
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.AuthRepository

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
