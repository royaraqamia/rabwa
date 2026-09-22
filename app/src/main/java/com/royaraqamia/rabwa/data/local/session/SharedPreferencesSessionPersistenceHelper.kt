package com.royaraqamia.rabwa.data.local.session

import android.content.Context
import android.content.SharedPreferences
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json

class SharedPreferencesSessionPersistenceHelper(
    private val context: Context,
    private val prefsName: String = PREFS_NAME,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) : SessionPersistenceHelper {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    override fun saveSession(session: UserSession) {
        try {
            val sessionJson = json.encodeToString(UserSession.serializer(), session)
            val expiresAtMillis = session.expiresAt.toEpochMilliseconds()

            prefs.edit()
                .putString(KEY_SESSION_DATA, sessionJson)
                .putString(KEY_ACCESS_TOKEN, session.accessToken)
                .putString(KEY_REFRESH_TOKEN, session.refreshToken)
                .putLong(KEY_EXPIRES_AT, expiresAtMillis)
                .putLong(KEY_SAVED_AT, System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {
            // Graceful error handling for persistence failures
        }
    }

    override fun loadSession(): UserSession? {
        val sessionJson = prefs.getString(KEY_SESSION_DATA, null) ?: return null
        return try {
            json.decodeFromString(UserSession.serializer(), sessionJson)
        } catch (_: Exception) {
            null
        }
    }

    override fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_DATA)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .remove(KEY_SAVED_AT)
            .apply()
    }

    override fun hasPersistedSession(): Boolean {
        val sessionData = prefs.getString(KEY_SESSION_DATA, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        return !sessionData.isNullOrBlank() || !refreshToken.isNullOrBlank()
    }

    override fun getSavedRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun isTokenExpired(): Boolean {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (expiresAt <= 0L) return false
        // Margin of 30 seconds before expiration
        return System.currentTimeMillis() >= (expiresAt - 30_000L)
    }

    companion object {
        private const val PREFS_NAME = "supabase_auth_session_prefs"
        private const val KEY_SESSION_DATA = "key_supabase_session_data"
        private const val KEY_ACCESS_TOKEN = "key_supabase_access_token"
        private const val KEY_REFRESH_TOKEN = "key_supabase_refresh_token"
        private const val KEY_EXPIRES_AT = "key_supabase_expires_at"
        private const val KEY_SAVED_AT = "key_supabase_saved_at"
    }
}
