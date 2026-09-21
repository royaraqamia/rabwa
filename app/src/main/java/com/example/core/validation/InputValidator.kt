package com.example.core.validation

import com.example.core.result.AppError
import com.example.core.result.AppResult

/**
 * Enterprise Zero-Trust Input Validator.
 * All inputs entering the domain layer must pass validation and sanitization.
 */
object InputValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val ALPHANUMERIC_REGEX = Regex("^[a-zA-Z0-9_-]+$")

    fun validateNotEmpty(field: String, value: String): AppResult<String> {
        val sanitized = value.trim()
        if (sanitized.isEmpty()) {
            return AppResult.Failure(
                AppError.Validation(field, "$field must not be empty.")
            )
        }
        return AppResult.Success(sanitized)
    }

    fun validateLength(
        field: String,
        value: String,
        minLength: Int = 1,
        maxLength: Int = 255
    ): AppResult<String> {
        val notEmptyCheck = validateNotEmpty(field, value)
        if (notEmptyCheck is AppResult.Failure) return notEmptyCheck

        val sanitized = (notEmptyCheck as AppResult.Success).data
        if (sanitized.length < minLength) {
            return AppResult.Failure(
                AppError.Validation(field, "$field must be at least $minLength characters.")
            )
        }
        if (sanitized.length > maxLength) {
            return AppResult.Failure(
                AppError.Validation(field, "$field cannot exceed $maxLength characters.")
            )
        }
        return AppResult.Success(sanitized)
    }

    fun validateEmail(field: String = "Email", email: String): AppResult<String> {
        val lengthCheck = validateLength(field, email, minLength = 5, maxLength = 100)
        if (lengthCheck is AppResult.Failure) return lengthCheck

        val sanitized = (lengthCheck as AppResult.Success).data
        if (!EMAIL_REGEX.matches(sanitized)) {
            return AppResult.Failure(
                AppError.Validation(field, "Invalid email address format.")
            )
        }
        return AppResult.Success(sanitized)
    }

    fun validateIdentifier(field: String, value: String): AppResult<String> {
        val lengthCheck = validateLength(field, value, minLength = 3, maxLength = 50)
        if (lengthCheck is AppResult.Failure) return lengthCheck

        val sanitized = (lengthCheck as AppResult.Success).data
        if (!ALPHANUMERIC_REGEX.matches(sanitized)) {
            return AppResult.Failure(
                AppError.Validation(field, "$field may only contain alphanumeric characters, hyphens, and underscores.")
            )
        }
        return AppResult.Success(sanitized)
    }
}
