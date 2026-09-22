package com.royaraqamia.rabwa.data.local.session

import android.content.Context
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession

/**
 * Enterprise SessionManager implementation for Supabase Auth plugin, delegating persistence
 * to SessionPersistenceHelper.
 */
class SharedPreferencesSessionManager(
    private val persistenceHelper: SessionPersistenceHelper
) : SessionManager {

    constructor(context: Context) : this(SharedPreferencesSessionPersistenceHelper(context.applicationContext))

    override suspend fun saveSession(session: UserSession) {
        persistenceHelper.saveSession(session)
    }

    override suspend fun loadSession(): UserSession? {
        return persistenceHelper.loadSession()
    }

    override suspend fun deleteSession() {
        persistenceHelper.clearSession()
    }
}
