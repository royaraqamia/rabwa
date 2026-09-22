package com.royaraqamia.rabwa.data.remote.datasource

import com.royaraqamia.rabwa.core.network.SupabaseClientProvider
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Interface isolating Supabase Auth SDK interactions from repository logic.
 */
interface AuthRemoteDataSource {
    fun observeSessionStatus(): Flow<SessionStatus>
    fun getCurrentUser(): UserInfo?
    fun getCurrentSession(): UserSession?
    suspend fun signUpWithEmail(email: String, password: String): AppResult<UserInfo>
    suspend fun signInWithEmail(email: String, password: String): AppResult<UserInfo>
    suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<UserInfo>
    suspend fun signOut(): AppResult<Unit>
    suspend fun resetPassword(email: String): AppResult<Unit>
    suspend fun refreshCurrentSession(): AppResult<UserInfo>
    suspend fun restoreSessionFromStorage(): AppResult<UserInfo?>
    suspend fun updateUserAvatar(avatarUrl: String): AppResult<UserInfo>
}

class SupabaseAuthRemoteDataSource(
    private val authProvider: () -> Auth = { SupabaseClientProvider.auth }
) : AuthRemoteDataSource {

    private val auth: Auth get() = authProvider()

    override fun observeSessionStatus(): Flow<SessionStatus> = auth.sessionStatus

    override fun getCurrentUser(): UserInfo? = auth.currentUserOrNull()

    override fun getCurrentSession(): UserSession? = auth.currentSessionOrNull()

    override suspend fun refreshCurrentSession(): AppResult<UserInfo> {
        return runCatching {
            auth.refreshCurrentSession()
            auth.currentUserOrNull()
        }.fold(
            onSuccess = { userInfo ->
                if (userInfo != null) {
                    AppResult.Success(userInfo)
                } else {
                    AppResult.Failure(AppError.Auth.invalidCredentials("Token refreshed but user info is missing."))
                }
            },
            onFailure = { throwable ->
                mapAuthException(throwable)
            }
        )
    }

    override suspend fun restoreSessionFromStorage(): AppResult<UserInfo?> {
        return runCatching {
            val hasSession = auth.loadFromStorage()
            if (hasSession) {
                auth.currentUserOrNull()
            } else {
                null
            }
        }.fold(
            onSuccess = { userInfo ->
                AppResult.Success(userInfo)
            },
            onFailure = { throwable ->
                mapAuthException(throwable)
            }
        )
    }

    override suspend fun signUpWithEmail(email: String, password: String): AppResult<UserInfo> {
        return runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            auth.currentUserOrNull()
        }.fold(
            onSuccess = { userInfo ->
                if (userInfo != null) {
                    AppResult.Success(userInfo)
                } else {
                    AppResult.Failure(AppError.Auth.invalidCredentials("Sign up completed but user info is missing."))
                }
            },
            onFailure = { throwable ->
                mapAuthException(throwable)
            }
        )
    }

    override suspend fun signInWithEmail(email: String, password: String): AppResult<UserInfo> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            auth.currentUserOrNull()
        }.fold(
            onSuccess = { userInfo ->
                if (userInfo != null) {
                    AppResult.Success(userInfo)
                } else {
                    AppResult.Failure(AppError.Auth.invalidCredentials("Sign in completed but user info is missing."))
                }
            },
            onFailure = { throwable ->
                mapAuthException(throwable)
            }
        )
    }

    override suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<UserInfo> {
        return runCatching {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
                this.nonce = rawNonce
            }
            auth.currentUserOrNull()
        }.fold(
            onSuccess = { userInfo ->
                if (userInfo != null) {
                    AppResult.Success(userInfo)
                } else {
                    AppResult.Failure(AppError.Auth.invalidCredentials("Google Sign-In completed but user info is missing."))
                }
            },
            onFailure = { throwable ->
                mapAuthException(throwable)
            }
        )
    }

    override suspend fun signOut(): AppResult<Unit> {
        return runCatching {
            auth.signOut()
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> mapAuthException(throwable) }
        )
    }

    override suspend fun resetPassword(email: String): AppResult<Unit> {
        return runCatching {
            auth.resetPasswordForEmail(email)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> mapAuthException(throwable) }
        )
    }

    override suspend fun updateUserAvatar(avatarUrl: String): AppResult<UserInfo> {
        return runCatching {
            val user = auth.currentUserOrNull()
            var updatedUserInfo: UserInfo? = null
            if (user != null) {
                updatedUserInfo = runCatching {
                    auth.updateUser {
                        data = kotlinx.serialization.json.buildJsonObject {
                            put("avatar_url", kotlinx.serialization.json.JsonPrimitive(avatarUrl))
                        }
                    }
                }.getOrNull()

                runCatching {
                    val dto = com.royaraqamia.rabwa.data.remote.dto.SupabaseProfileDto(
                        id = user.id,
                        email = user.email,
                        avatarUrl = avatarUrl
                    )
                    SupabaseClientProvider.postgrest.from("profiles").upsert(dto)
                }
            }
            updatedUserInfo ?: auth.currentUserOrNull()
        }.fold(
            onSuccess = { userInfo ->
                if (userInfo != null) {
                    AppResult.Success(userInfo)
                } else {
                    AppResult.Failure(AppError.Auth(message = "Avatar updated but current user state is null."))
                }
            },
            onFailure = { throwable ->
                mapAuthException(throwable)
            }
        )
    }

    private fun <T> mapAuthException(throwable: Throwable): AppResult<T> {
        val errorMsg = throwable.message ?: "Authentication operation failed"
        return when {
            errorMsg.contains("invalid", ignoreCase = true) || errorMsg.contains("credentials", ignoreCase = true) -> {
                AppResult.Failure(AppError.Auth.invalidCredentials(errorMsg))
            }
            errorMsg.contains("already registered", ignoreCase = true) || errorMsg.contains("user_already_exists", ignoreCase = true) -> {
                AppResult.Failure(AppError.Auth.userAlreadyExists(errorMsg))
            }
            errorMsg.contains("network", ignoreCase = true) || errorMsg.contains("connect", ignoreCase = true) -> {
                AppResult.Failure(AppError.Network(message = errorMsg))
            }
            else -> {
                AppResult.Failure(AppError.Auth(message = errorMsg))
            }
        }
    }
}
