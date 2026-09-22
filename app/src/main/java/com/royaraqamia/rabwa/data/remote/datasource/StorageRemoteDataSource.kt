package com.royaraqamia.rabwa.data.remote.datasource

import com.royaraqamia.rabwa.core.network.SupabaseClientProvider
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import io.github.jan.supabase.storage.FileObject
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.upload

/**
 * Interface isolating Supabase Storage SDK interactions from business logic.
 */
interface StorageRemoteDataSource {
    suspend fun uploadFile(
        bucket: String,
        path: String,
        data: ByteArray,
        upsert: Boolean = true
    ): AppResult<String>

    fun getPublicUrl(bucket: String, path: String): AppResult<String>

    suspend fun deleteFile(bucket: String, path: String): AppResult<Unit>

    suspend fun listFiles(bucket: String, pathPrefix: String? = null): AppResult<List<FileObject>>
}

class SupabaseStorageRemoteDataSource(
    private val storageProvider: () -> Storage = { SupabaseClientProvider.storage }
) : StorageRemoteDataSource {

    private val storage: Storage get() = storageProvider()

    override suspend fun uploadFile(
        bucket: String,
        path: String,
        data: ByteArray,
        upsert: Boolean
    ): AppResult<String> {
        return runCatching {
            storage.from(bucket).upload(path, data) {
                this.upsert = upsert
            }
            storage.from(bucket).publicUrl(path)
        }.fold(
            onSuccess = { url -> AppResult.Success(url) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.Storage(
                        bucket = bucket,
                        path = path,
                        message = throwable.localizedMessage ?: "Failed to upload file to '$bucket/$path'"
                    )
                )
            }
        )
    }

    override fun getPublicUrl(bucket: String, path: String): AppResult<String> {
        return runCatching {
            storage.from(bucket).publicUrl(path)
        }.fold(
            onSuccess = { url -> AppResult.Success(url) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.Storage(
                        bucket = bucket,
                        path = path,
                        message = throwable.localizedMessage ?: "Failed to generate public URL for '$bucket/$path'"
                    )
                )
            }
        )
    }

    override suspend fun deleteFile(bucket: String, path: String): AppResult<Unit> {
        return runCatching {
            storage.from(bucket).delete(listOf(path))
            Unit
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.Storage(
                        bucket = bucket,
                        path = path,
                        message = throwable.localizedMessage ?: "Failed to delete file from '$bucket/$path'"
                    )
                )
            }
        )
    }

    override suspend fun listFiles(
        bucket: String,
        pathPrefix: String?
    ): AppResult<List<FileObject>> {
        return runCatching {
            storage.from(bucket).list(pathPrefix ?: "")
        }.fold(
            onSuccess = { list -> AppResult.Success(list) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.Storage(
                        bucket = bucket,
                        path = pathPrefix,
                        message = throwable.localizedMessage ?: "Failed to list files from '$bucket'"
                    )
                )
            }
        )
    }
}
