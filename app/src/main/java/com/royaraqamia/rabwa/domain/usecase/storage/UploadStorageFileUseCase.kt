package com.royaraqamia.rabwa.domain.usecase.storage

import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.core.validation.AuthValidator
import com.royaraqamia.rabwa.domain.model.storage.RemoteFile
import com.royaraqamia.rabwa.domain.model.storage.UploadFileRequest
import com.royaraqamia.rabwa.domain.repository.StorageRepository

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
