package com.example.domain.usecase.storage

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.core.validation.AuthValidator
import com.example.domain.model.storage.RemoteFile
import com.example.domain.model.storage.UploadFileRequest
import com.example.domain.repository.StorageRepository

class UploadStorageFileUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(request: UploadFileRequest): AppResult<RemoteFile> {
        val bucketValidation = AuthValidator.validateBucketName(request.bucket)
        if (bucketValidation is AppResult.Failure) return bucketValidation

        val pathValidation = AuthValidator.sanitizeStoragePath(request.path)
        if (pathValidation is AppResult.Failure) return pathValidation

        if (request.data.isEmpty()) {
            return AppResult.Failure(
                AppError.Validation("data", "File payload cannot be empty.")
            )
        }

        val sanitizedRequest = request.copy(
            bucket = (bucketValidation as AppResult.Success).data,
            path = (pathValidation as AppResult.Success).data
        )

        return storageRepository.uploadFile(sanitizedRequest)
    }
}
