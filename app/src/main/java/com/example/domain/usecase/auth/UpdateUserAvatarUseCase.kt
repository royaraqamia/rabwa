package com.example.domain.usecase.auth

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.auth.AuthUser
import com.example.domain.model.storage.UploadFileRequest
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.StorageRepository

class UpdateUserAvatarUseCase(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) {

    suspend fun uploadAndSaveAvatar(
        userId: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): AppResult<AuthUser> {
        val fileName = "avatar_${userId}_${System.currentTimeMillis()}.jpg"
        val path = "avatars/$fileName"

        val uploadRequest = UploadFileRequest(
            bucket = "documents",
            path = path,
            data = imageBytes,
            mimeType = mimeType
        )

        return when (val uploadResult = storageRepository.uploadFile(uploadRequest)) {
            is AppResult.Success -> {
                val publicUrl = uploadResult.data.publicUrl
                if (!publicUrl.isNullOrBlank()) {
                    authRepository.updateUserAvatar(publicUrl)
                } else {
                    AppResult.Failure(AppError.Storage(bucket = "documents", path = path, message = "Uploaded image public URL is empty."))
                }
            }
            is AppResult.Failure -> uploadResult
        }
    }

    suspend fun saveAvatarUrl(avatarUrl: String): AppResult<AuthUser> {
        return authRepository.updateUserAvatar(avatarUrl)
    }
}
