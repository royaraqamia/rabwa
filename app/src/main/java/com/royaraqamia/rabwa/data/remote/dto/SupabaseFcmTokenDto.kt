package com.royaraqamia.rabwa.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for `public.user_fcm_tokens` table in Supabase.
 */
@Serializable
data class SupabaseFcmTokenDto(
    @SerialName("token")
    val token: String,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("device_model")
    val deviceModel: String? = null,
    @SerialName("app_version")
    val appVersion: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
