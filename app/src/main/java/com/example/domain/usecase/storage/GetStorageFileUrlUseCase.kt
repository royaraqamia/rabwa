package com.example.domain.usecase.storage

import com.example.core.result.AppResult
import com.example.core.validation.AuthValidator
import com.example.domain.repository.StorageRepository

class GetStorageFileUrlUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(bucket: String, path: String): AppResult<String> {
        val bucketValidation = AuthValidator.validateBucketName(bucket)
        if (bucketValidation is AppResult.Failure) return bucketValidation

        val pathValidation = AuthValidator.sanitizeStoragePath(path)
        if (pathValidation is AppResult.Failure) return pathValidation

        return storageRepository.getPublicUrl(
            bucket = (bucketValidation as AppResult.Success).data,
            path = (pathValidation as AppResult.Success).data
        )
    }
}
