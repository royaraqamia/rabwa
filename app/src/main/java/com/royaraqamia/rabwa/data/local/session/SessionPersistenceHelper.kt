package com.royaraqamia.rabwa.data.local.session

import io.github.jan.supabase.auth.user.UserSession

/**
 * Enterprise session persistence contract for storing and recovering Supabase authentication sessions
 * across application restarts.
 */
interface SessionPersistenceHelper {
    fun saveSession(session: UserSession)
    fun loadSession(): UserSession?
    fun clearSession()
    fun hasPersistedSession(): Boolean
    fun getSavedRefreshToken(): String?
    fun isTokenExpired(): Boolean
}
