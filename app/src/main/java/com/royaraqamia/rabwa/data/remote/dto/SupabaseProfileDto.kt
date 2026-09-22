package com.royaraqamia.rabwa.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfileDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
