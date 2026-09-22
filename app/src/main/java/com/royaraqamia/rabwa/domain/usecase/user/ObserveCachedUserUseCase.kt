package com.royaraqamia.rabwa.domain.usecase.user

import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveCachedUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<AuthUser?> {
        return userRepository.observeCurrentUser()
    }
}
