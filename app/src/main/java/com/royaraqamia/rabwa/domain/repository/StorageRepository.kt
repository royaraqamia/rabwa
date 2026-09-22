package com.royaraqamia.rabwa.domain.repository

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.storage.RemoteFile
import com.royaraqamia.rabwa.domain.model.storage.UploadFileRequest

/**
 * Enterprise contract for Supabase Storage bucket operations.
 */
interface StorageRepository {
    suspend fun uploadFile(request: UploadFileRequest): AppResult<RemoteFile>
    suspend fun getPublicUrl(bucket: String, path: String): AppResult<String>
    suspend fun deleteFile(bucket: String, path: String): AppResult<Unit>
    suspend fun listFiles(bucket: String, pathPrefix: String? = null): AppResult<List<RemoteFile>>
}
