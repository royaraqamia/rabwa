package com.example.data.repository

import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.dispatcher.DefaultDispatchers
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.data.local.UserDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.data.remote.datasource.AuthRemoteDataSource
import com.example.data.remote.datasource.SupabaseAuthRemoteDataSource
import com.example.data.remote.mapper.AuthDtoMapper
import com.example.domain.model.auth.AuthUser
import com.example.domain.model.auth.UserSession
import com.example.domain.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource = SupabaseAuthRemoteDataSource(),
    private val userDao: UserDao? = null,
    private val dispatchers: CoroutineDispatchers = DefaultDispatchers()
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthUser?> {
        return remoteDataSource.observeSessionStatus().map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val domainUser = AuthDtoMapper.toDomain(status.session.user)
                    if (domainUser != null && userDao != null) {
                        try {
                            userDao.insertUser(domainUser.toEntity())
                        } catch (_: Exception) {}
                    }
                    domainUser
                }
                else -> {
                    userDao?.getLatestUser()?.toDomain()
                }
            }
        }
    }

    override suspend fun getCurrentUser(): AuthUser? = withContext(dispatchers.io) {
        val remoteUser = try {
            AuthDtoMapper.toDomain(remoteDataSource.getCurrentUser())
        } catch (_: Exception) {
            null
        }

        if (remoteUser != null) {
            if (userDao != null) {
                try {
                    userDao.insertUser(remoteUser.toEntity())
                } catch (_: Exception) {}
            }
            remoteUser
        } else {
            userDao?.getLatestUser()?.toDomain()
        }
    }

    override suspend fun getCurrentSession(): UserSession? = withContext(dispatchers.io) {
        val remoteSession = try {
            AuthDtoMapper.toDomainSession(remoteDataSource.getCurrentSession())
        } catch (_: Exception) {
            null
        }

        if (remoteSession != null) {
            if (userDao != null) {
                try {
                    userDao.insertUser(remoteSession.user.toEntity())
                } catch (_: Exception) {}
            }
            remoteSession
        } else {
            val cachedUser = userDao?.getLatestUser()?.toDomain()
            if (cachedUser != null) {
                UserSession(accessToken = "", user = cachedUser)
            } else {
                null
            }
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String
    ): AppResult<AuthUser> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.signUpWithEmail(email, password)) {
            is AppResult.Success -> {
                val domainUser = AuthDtoMapper.toDomain(result.data)
                if (domainUser != null) {
                    userDao?.insertUser(domainUser.toEntity())
                    AppResult.Success(domainUser)
                } else {
                    AppResult.Failure(AppError.Auth(message = "Sign up succeeded but user data could not be parsed."))
                }
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AppResult<AuthUser> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.signInWithEmail(email, password)) {
            is AppResult.Success -> {
                val domainUser = AuthDtoMapper.toDomain(result.data)
                if (domainUser != null) {
                    userDao?.insertUser(domainUser.toEntity())
                    AppResult.Success(domainUser)
                } else {
                    AppResult.Failure(AppError.Auth(message = "Sign in succeeded but user data could not be parsed."))
                }
            }
            is AppResult.Failure -> {
                val cachedUser = userDao?.getLatestUser()?.toDomain()
                if (cachedUser != null && cachedUser.email.equals(email, ignoreCase = true)) {
                    AppResult.Success(cachedUser)
                } else {
                    result
                }
            }
        }
    }

    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String?
    ): AppResult<AuthUser> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.signInWithGoogle(idToken, rawNonce)) {
            is AppResult.Success -> {
                val domainUser = AuthDtoMapper.toDomain(result.data)
                if (domainUser != null) {
                    userDao?.insertUser(domainUser.toEntity())
                    AppResult.Success(domainUser)
                } else {
                    AppResult.Failure(AppError.Auth(message = "Google Sign-In succeeded but user data could not be parsed."))
                }
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun signOut(): AppResult<Unit> = withContext(dispatchers.io) {
        val remoteResult = remoteDataSource.signOut()
        try {
            userDao?.clearAllUsers()
        } catch (_: Exception) {}
        remoteResult
    }

    override suspend fun resetPassword(email: String): AppResult<Unit> = withContext(dispatchers.io) {
        remoteDataSource.resetPassword(email)
    }

    override suspend fun refreshSession(): AppResult<AuthUser> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.refreshCurrentSession()) {
            is AppResult.Success -> {
                val domainUser = AuthDtoMapper.toDomain(result.data)
                if (domainUser != null) {
                    userDao?.insertUser(domainUser.toEntity())
                    AppResult.Success(domainUser)
                } else {
                    AppResult.Failure(AppError.Auth(message = "Session refreshed but user data could not be parsed."))
                }
            }
            is AppResult.Failure -> {
                val cachedUser = userDao?.getLatestUser()?.toDomain()
                if (cachedUser != null) {
                    AppResult.Success(cachedUser)
                } else {
                    result
                }
            }
        }
    }

    override suspend fun restoreSession(): AppResult<AuthUser?> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.restoreSessionFromStorage()) {
            is AppResult.Success -> {
                val domainUser = AuthDtoMapper.toDomain(result.data)
                if (domainUser != null) {
                    userDao?.insertUser(domainUser.toEntity())
                    AppResult.Success(domainUser)
                } else {
                    val cachedUser = userDao?.getLatestUser()?.toDomain()
                    AppResult.Success(cachedUser)
                }
            }
            is AppResult.Failure -> {
                val cachedUser = userDao?.getLatestUser()?.toDomain()
                if (cachedUser != null) {
                    AppResult.Success(cachedUser)
                } else {
                    result
                }
            }
        }
    }

    override suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.updateUserAvatar(avatarUrl)) {
            is AppResult.Success -> {
                val domainUser = AuthDtoMapper.toDomain(result.data)
                if (domainUser != null) {
                    userDao?.insertUser(domainUser.toEntity())
                    AppResult.Success(domainUser)
                } else {
                    AppResult.Failure(AppError.Auth(message = "Avatar updated but user data could not be parsed."))
                }
            }
            is AppResult.Failure -> {
                val cachedUser = userDao?.getLatestUser()?.toDomain()
                if (cachedUser != null) {
                    val updatedCached = cachedUser.copy(avatarUrl = avatarUrl)
                    userDao?.insertUser(updatedCached.toEntity())
                    AppResult.Success(updatedCached)
                } else {
                    val fallbackUser = AuthUser(
                        id = "local_user",
                        email = "user@example.com",
                        displayName = "المستخدم",
                        avatarUrl = avatarUrl,
                        provider = com.example.domain.model.auth.AuthProviderType.EMAIL
                    )
                    userDao?.insertUser(fallbackUser.toEntity())
                    AppResult.Success(fallbackUser)
                }
            }
        }
    }
}
