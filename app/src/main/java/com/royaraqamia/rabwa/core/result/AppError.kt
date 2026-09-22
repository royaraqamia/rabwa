package com.royaraqamia.rabwa.core.result

/**
 * Enterprise error hierarchy for structured, predictable error handling without leaking exceptions.
 */
sealed interface AppError {
    val message: String

    data class Network(
        val statusCode: Int? = null,
        override val message: String = "Network connection failed",
        val isNoInternet: Boolean = false
    ) : AppError {
        companion object {
            fun noInternet(): Network = Network(statusCode = null, message = "No Internet", isNoInternet = true)
        }
    }

    data class Validation(
        val field: String,
        override val message: String
    ) : AppError

    data class Database(
        override val message: String = "Local persistence error occurred"
    ) : AppError

    data class NotFound(
        val resource: String,
        override val message: String = "Requested resource '$resource' was not found"
    ) : AppError

    data class Unauthorized(
        override val message: String = "Unauthorized operation"
    ) : AppError

    data class Auth(
        val code: String? = null,
        override val message: String = "Authentication failed"
    ) : AppError {
        companion object {
            fun invalidCredentials(message: String = "Invalid email or password"): Auth =
                Auth(code = "invalid_credentials", message = message)
            fun userAlreadyExists(message: String = "User with this email already exists"): Auth =
                Auth(code = "user_already_exists", message = message)
            fun sessionExpired(message: String = "Session has expired, please log in again"): Auth =
                Auth(code = "session_expired", message = message)
            fun configurationMissing(message: String = "Supabase configuration is not configured"): Auth =
                Auth(code = "config_missing", message = message)
        }
    }

    data class RemoteDatabase(
        val code: String? = null,
        val details: String? = null,
        override val message: String = "Remote database operation failed"
    ) : AppError

    data class Storage(
        val bucket: String? = null,
        val path: String? = null,
        override val message: String = "Storage operation failed"
    ) : AppError

    data class Unknown(
        val cause: Throwable? = null,
        override val message: String = cause?.localizedMessage ?: "An unexpected error occurred"
    ) : AppError
}
