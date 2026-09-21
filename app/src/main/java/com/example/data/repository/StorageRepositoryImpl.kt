package com.example.data.repository

import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.dispatcher.DefaultDispatchers
import com.example.core.result.AppResult
import com.example.data.remote.datasource.StorageRemoteDataSource
import com.example.data.remote.datasource.SupabaseStorageRemoteDataSource
import com.example.data.remote.mapper.StorageMapper
import com.example.domain.model.storage.RemoteFile
import com.example.domain.model.storage.UploadFileRequest
import com.example.domain.repository.StorageRepository
import kotlinx.coroutines.withContext

class StorageRepositoryImpl(
    private val remoteDataSource: StorageRemoteDataSource = SupabaseStorageRemoteDataSource(),
    private val dispatchers: CoroutineDispatchers = DefaultDispatchers()
) : StorageRepository {

    override suspend fun uploadFile(
        request: UploadFileRequest
    ): AppResult<RemoteFile> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.uploadFile(
            bucket = request.bucket,
            path = request.path,
            data = request.data,
            upsert = request.upsert
        )) {
            is AppResult.Success -> {
                val remoteFile = RemoteFile(
                    id = request.path,
                    name = request.path.substringAfterLast('/'),
                    bucket = request.bucket,
                    path = request.path,
                    sizeBytes = request.data.size.toLong(),
                    mimeType = request.mimeType,
                    publicUrl = result.data,
                    createdAt = System.currentTimeMillis().toString()
                )
                AppResult.Success(remoteFile)
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun getPublicUrl(
        bucket: String,
        path: String
    ): AppResult<String> = withContext(dispatchers.io) {
        remoteDataSource.getPublicUrl(bucket, path)
    }

    override suspend fun deleteFile(
        bucket: String,
        path: String
    ): AppResult<Unit> = withContext(dispatchers.io) {
        remoteDataSource.deleteFile(bucket, path)
    }

    override suspend fun listFiles(
        bucket: String,
        pathPrefix: String?
    ): AppResult<List<RemoteFile>> = withContext(dispatchers.io) {
        when (val result = remoteDataSource.listFiles(bucket, pathPrefix)) {
            is AppResult.Success -> {
                val files = result.data.map { fileObj ->
                    val urlResult = remoteDataSource.getPublicUrl(bucket, fileObj.name)
                    val publicUrl = if (urlResult is AppResult.Success) urlResult.data else null
                    StorageMapper.toDomain(fileObj, bucket, publicUrl)
                }
                AppResult.Success(files)
            }
            is AppResult.Failure -> result
        }
    }
}
