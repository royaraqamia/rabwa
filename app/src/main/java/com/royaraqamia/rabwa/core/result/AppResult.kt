package com.royaraqamia.rabwa.core.result

/**
 * Type-safe functional Result monad for clean architecture boundaries.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    companion object {
        inline fun <T> runCatching(block: () -> T): AppResult<T> {
            return try {
                Success(block())
            } catch (t: Throwable) {
                Failure(AppError.Unknown(cause = t))
            }
        }
    }
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (AppError) -> R
): R = when (this) {
    is AppResult.Success -> onSuccess(data)
    is AppResult.Failure -> onFailure(error)
}
