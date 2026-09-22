package com.royaraqamia.rabwa.domain.repository

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrentUser(): Flow<AuthUser?>
    fun observeUserById(userId: String): Flow<AuthUser?>
    suspend fun getCachedUser(userId: String): AuthUser?
    suspend fun getLatestCachedUser(): AuthUser?
    suspend fun cacheUser(user: AuthUser): AppResult<Unit>
    suspend fun clearUserCache(): AppResult<Unit>
}
