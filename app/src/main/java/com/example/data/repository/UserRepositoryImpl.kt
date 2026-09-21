package com.example.data.repository

import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.dispatcher.DefaultDispatchers
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.data.local.UserDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.auth.AuthUser
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val dispatchers: CoroutineDispatchers = DefaultDispatchers()
) : UserRepository {

    override fun observeCurrentUser(): Flow<AuthUser?> {
        return userDao.observeLatestUser()
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)
    }

    override fun observeUserById(userId: String): Flow<AuthUser?> {
        return userDao.observeUserById(userId)
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun getCachedUser(userId: String): AuthUser? = withContext(dispatchers.io) {
        userDao.getUserById(userId)?.toDomain()
    }

    override suspend fun getLatestCachedUser(): AuthUser? = withContext(dispatchers.io) {
        userDao.getLatestUser()?.toDomain()
    }

    override suspend fun cacheUser(user: AuthUser): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            userDao.insertUser(user.toEntity())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Database("فشل حفظ بيانات المستخدم محلياً: ${e.message}"))
        }
    }

    override suspend fun clearUserCache(): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            userDao.clearAllUsers()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Database("فشل مسح ذاكرة التخزين المؤقت للمستخدم: ${e.message}"))
        }
    }
}
