package com.royaraqamia.rabwa.domain.repository

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.model.auth.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Enterprise contract for Supabase Authentication operations (Email/Password & Google OAuth).
 */
interface AuthRepository {
    fun observeAuthState(): Flow<AuthUser?>
    suspend fun getCurrentUser(): AuthUser?
    suspend fun getCurrentSession(): UserSession?
    suspend fun signUpWithEmail(email: String, password: String): AppResult<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser>
    suspend fun signInWithGoogle(idToken: String, rawNonce: String? = null): AppResult<AuthUser>
    suspend fun signOut(): AppResult<Unit>
    suspend fun resetPassword(email: String): AppResult<Unit>
    suspend fun refreshSession(): AppResult<AuthUser>
    suspend fun restoreSession(): AppResult<AuthUser?>
    suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser>
}
