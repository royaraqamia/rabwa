package com.royaraqamia.rabwa.presentation.common

import com.royaraqamia.rabwa.core.result.AppError

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>

    val isLoading: Boolean
        get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}
