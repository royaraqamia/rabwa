package com.royaraqamia.rabwa.domain.usecase.storage

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.core.validation.AuthValidator
import com.royaraqamia.rabwa.domain.repository.StorageRepository

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
