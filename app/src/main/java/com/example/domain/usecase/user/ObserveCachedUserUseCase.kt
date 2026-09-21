package com.example.domain.usecase.user

import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveCachedUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<AuthUser?> {
        return userRepository.observeCurrentUser()
    }
}
