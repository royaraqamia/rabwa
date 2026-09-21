package com.example.domain.repository

import com.example.core.result.AppResult
import com.example.domain.model.storage.RemoteFile
import com.example.domain.model.storage.UploadFileRequest

/**
 * Enterprise contract for Supabase Storage bucket operations.
 */
interface StorageRepository {
    suspend fun uploadFile(request: UploadFileRequest): AppResult<RemoteFile>
    suspend fun getPublicUrl(bucket: String, path: String): AppResult<String>
    suspend fun deleteFile(bucket: String, path: String): AppResult<Unit>
    suspend fun listFiles(bucket: String, pathPrefix: String? = null): AppResult<List<RemoteFile>>
}
