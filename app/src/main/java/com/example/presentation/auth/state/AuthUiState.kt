package com.example.presentation.auth.state

import com.example.core.result.AppError
import com.example.domain.model.auth.AuthUser

sealed interface AuthUiState {
    data object Unauthenticated : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: AuthUser) : AuthUiState
    data class Error(val error: AppError) : AuthUiState
}
