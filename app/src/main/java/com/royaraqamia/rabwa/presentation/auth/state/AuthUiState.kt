package com.royaraqamia.rabwa.presentation.auth.state

import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.domain.model.auth.AuthUser

sealed interface AuthUiState {
    data object Unauthenticated : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: AuthUser) : AuthUiState
    data class Error(val error: AppError) : AuthUiState
}
