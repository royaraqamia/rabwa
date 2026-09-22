package com.royaraqamia.rabwa.domain.usecase.auth

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> = authRepository.signOut()
}
