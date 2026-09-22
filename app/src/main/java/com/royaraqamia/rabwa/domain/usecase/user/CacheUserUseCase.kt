package com.royaraqamia.rabwa.domain.usecase.user

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.UserRepository

class CacheUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: AuthUser): AppResult<Unit> {
        return userRepository.cacheUser(user)
    }
}
