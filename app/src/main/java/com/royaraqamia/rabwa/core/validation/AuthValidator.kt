package com.royaraqamia.rabwa.core.validation

import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult

/**
 * Enterprise Zero-Trust Validator for Authentication, Database, and Storage operations.
 */
object AuthValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val BUCKET_NAME_REGEX = Regex("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$")

    fun validateEmail(email: String): AppResult<String> {
        val sanitized = email.trim()
        if (sanitized.isEmpty()) {
            return AppResult.Failure(AppError.Validation("email", "Email address cannot be empty."))
        }
        if (sanitized.length > 254) {
            return AppResult.Failure(AppError.Validation("email", "Email address is too long."))
        }
        if (sanitized.contains("..") || sanitized.startsWith("@") || sanitized.endsWith("@")) {
            return AppResult.Failure(AppError.Validation("email", "Invalid email address format."))
        }
        if (!EMAIL_REGEX.matches(sanitized)) {
            return AppResult.Failure(AppError.Validation("email", "Please enter a valid email address."))
        }
        return AppResult.Success(sanitized)
    }

    fun validatePassword(password: String, minLength: Int = 6): AppResult<String> {
        if (password.isEmpty()) {
            return AppResult.Failure(AppError.Validation("password", "Password cannot be empty."))
        }
        if (password.length < minLength) {
            return AppResult.Failure(
                AppError.Validation("password", "Password must be at least $minLength characters.")
            )
        }
        if (password.length > 128) {
            return AppResult.Failure(
                AppError.Validation("password", "Password cannot exceed 128 characters.")
            )
        }
        return AppResult.Success(password)
    }

    fun validateIdToken(idToken: String): AppResult<String> {
        val sanitized = idToken.trim()
        if (sanitized.isEmpty()) {
            return AppResult.Failure(AppError.Validation("idToken", "Google ID token cannot be empty."))
        }
        // Minimal JWT sanity check (three dot-separated segments)
        val parts = sanitized.split(".")
        if (parts.size != 3) {
            return AppResult.Failure(AppError.Validation("idToken", "Invalid Google ID token format."))
        }
        return AppResult.Success(sanitized)
    }

    fun validateBucketName(bucket: String): AppResult<String> {
        val trimmed = bucket.trim()
        if (trimmed.isEmpty()) {
            return AppResult.Failure(AppError.Validation("bucket", "Storage bucket name cannot be empty."))
        }
        if (trimmed != trimmed.lowercase()) {
            return AppResult.Failure(AppError.Validation("bucket", "Storage bucket name must be all lowercase."))
        }
        if (trimmed.length < 3 || trimmed.length > 63) {
            return AppResult.Failure(AppError.Validation("bucket", "Storage bucket name must be between 3 and 63 characters."))
        }
        if (!BUCKET_NAME_REGEX.matches(trimmed)) {
            return AppResult.Failure(
                AppError.Validation("bucket", "Storage bucket name must be 3-63 lowercase alphanumeric characters, dots or hyphens.")
            )
        }
        return AppResult.Success(trimmed)
    }

    fun sanitizeStoragePath(path: String): AppResult<String> {
        val trimmed = path.trim().replace('\\', '/')
        if (trimmed.isEmpty()) {
            return AppResult.Failure(AppError.Validation("path", "Storage path cannot be empty."))
        }
        if (trimmed.contains("..")) {
            return AppResult.Failure(AppError.Validation("path", "Storage path contains illegal traversal characters."))
        }
        val cleanPath = trimmed.split("/").filter { it.isNotBlank() }.joinToString("/")
        if (cleanPath.isEmpty()) {
            return AppResult.Failure(AppError.Validation("path", "Storage path is invalid."))
        }
        return AppResult.Success(cleanPath)
    }
}
