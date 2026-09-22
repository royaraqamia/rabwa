package com.royaraqamia.rabwa.data.repository

import com.royaraqamia.rabwa.core.dispatcher.CoroutineDispatchers
import com.royaraqamia.rabwa.core.dispatcher.DefaultDispatchers
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.data.local.UserDao
import com.royaraqamia.rabwa.data.mapper.toDomain
import com.royaraqamia.rabwa.data.mapper.toEntity
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.repository.UserRepository
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
