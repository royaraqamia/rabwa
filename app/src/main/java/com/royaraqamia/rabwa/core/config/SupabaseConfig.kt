package com.royaraqamia.rabwa.core.config

import com.royaraqamia.rabwa.BuildConfig
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult

/**
 * Validated Supabase configuration holder.
 * Strictly reads from BuildConfig with zero-trust validation.
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
    val googleWebClientId: String? = null
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && 
                anonKey.isNotBlank() && 
                url.startsWith("http") && 
                !url.contains("your-project")

    companion object {
        fun loadFromBuildConfig(): SupabaseConfig {
            val url = runCatching { BuildConfig.SUPABASE_URL }.getOrDefault("").trim()
            val anonKey = runCatching { BuildConfig.SUPABASE_ANON_KEY }.getOrDefault("").trim()
            val googleClientId = runCatching { BuildConfig.GOOGLE_WEB_CLIENT_ID }.getOrDefault("").trim()
                .ifEmpty { null }

            return SupabaseConfig(
                url = url,
                anonKey = anonKey,
                googleWebClientId = googleClientId
            )
        }

        fun validate(config: SupabaseConfig): AppResult<SupabaseConfig> {
            if (config.url.isBlank()) {
                return AppResult.Failure(
                    AppError.Auth.configurationMissing("SUPABASE_URL is missing or empty in environment.")
                )
            }
            if (!config.url.startsWith("https://") && !config.url.startsWith("http://")) {
                return AppResult.Failure(
                    AppError.Auth.configurationMissing("SUPABASE_URL must be a valid HTTP/HTTPS URL.")
                )
            }
            if (config.anonKey.isBlank()) {
                return AppResult.Failure(
                    AppError.Auth.configurationMissing("SUPABASE_ANON_KEY is missing or empty in environment.")
                )
            }
            return AppResult.Success(config)
        }
    }
}
