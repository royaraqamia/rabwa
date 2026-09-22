package com.royaraqamia.rabwa.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseRecordDto(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null
)
