package com.example.domain.model.storage

data class RemoteFile(
    val id: String,
    val name: String,
    val bucket: String,
    val path: String,
    val sizeBytes: Long? = null,
    val mimeType: String? = null,
    val publicUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
