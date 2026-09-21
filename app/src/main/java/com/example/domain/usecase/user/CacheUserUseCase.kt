package com.example.domain.usecase.user

import com.example.core.result.AppResult
import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.UserRepository

class CacheUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: AuthUser): AppResult<Unit> {
        return userRepository.cacheUser(user)
    }
}
