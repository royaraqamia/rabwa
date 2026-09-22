package com.royaraqamia.rabwa.domain.usecase.storage

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.core.validation.AuthValidator
import com.royaraqamia.rabwa.domain.model.storage.RemoteFile
import com.royaraqamia.rabwa.domain.repository.StorageRepository

class ListStorageFilesUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(bucket: String, pathPrefix: String? = null): AppResult<List<RemoteFile>> {
        val bucketValidation = AuthValidator.validateBucketName(bucket)
        if (bucketValidation is AppResult.Failure) return bucketValidation

        val sanitizedPrefix = pathPrefix?.let { prefix ->
            val prefixCheck = AuthValidator.sanitizeStoragePath(prefix)
            if (prefixCheck is AppResult.Failure) return prefixCheck
            (prefixCheck as AppResult.Success).data
        }

        return storageRepository.listFiles(
            bucket = (bucketValidation as AppResult.Success).data,
            pathPrefix = sanitizedPrefix
        )
    }
}
