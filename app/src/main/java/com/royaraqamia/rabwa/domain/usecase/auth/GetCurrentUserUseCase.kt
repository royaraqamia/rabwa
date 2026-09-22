package com.royaraqamia.rabwa.domain.usecase.auth

import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthUser? = authRepository.getCurrentUser()
}
