package com.example.core.network

import com.example.core.config.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Enterprise Supabase Client Provider singleton.
 * Provides thread-safe, lazy initialization of SupabaseClient configured with
 * Auth (GoTrue), PostgREST, Storage, and Realtime plugins.
 */
object SupabaseClientProvider {

    @Volatile
    private var instance: SupabaseClient? = null
    private var customConfig: SupabaseConfig? = null
    private var sessionManager: io.github.jan.supabase.auth.SessionManager? = null

    fun setSessionManager(sessionManager: io.github.jan.supabase.auth.SessionManager) {
        synchronized(this) {
            this.sessionManager = sessionManager
            instance = null
        }
    }

    fun setConfig(config: SupabaseConfig) {
        synchronized(this) {
            customConfig = config
            instance = null
        }
    }

    fun getClient(): SupabaseClient {
        return instance ?: synchronized(this) {
            instance ?: createClient().also { instance = it }
        }
    }

    val auth: Auth
        get() = getClient().auth

    val postgrest: Postgrest
        get() = getClient().postgrest

    val storage: Storage
        get() = getClient().storage

    val realtime: Realtime
        get() = getClient().realtime

    private fun createClient(): SupabaseClient {
        val config = customConfig ?: SupabaseConfig.loadFromBuildConfig()
        val url = if (config.url.isNotBlank()) config.url else "https://localhost.supabase.co"
        val key = if (config.anonKey.isNotBlank()) config.anonKey else "dummy-anon-key"

        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            install(Auth) {
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
                this@SupabaseClientProvider.sessionManager?.let { customSessionManager ->
                    this.sessionManager = customSessionManager
                }
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
}
