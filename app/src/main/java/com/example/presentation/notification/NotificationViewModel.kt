package com.example.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.result.AppResult
import com.example.domain.model.notification.NotificationRegistrationStatus
import com.example.domain.usecase.notification.ObserveNotificationStatusUseCase
import com.example.domain.usecase.notification.RegisterPushTokenUseCase
import com.example.domain.usecase.notification.UnregisterPushTokenUseCase
import com.example.presentation.error.ErrorMessageResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Enterprise ViewModel for managing FCM and Supabase push notification device state.
 */
class NotificationViewModel(
    private val registerPushTokenUseCase: RegisterPushTokenUseCase,
    private val unregisterPushTokenUseCase: UnregisterPushTokenUseCase,
    private val observeNotificationStatusUseCase: ObserveNotificationStatusUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        observeStatus()
        syncPendingToken()
    }

    private fun syncPendingToken() {
        viewModelScope.launch(dispatchers.io) {
            registerPushTokenUseCase.syncPendingToken()
        }
    }

    private fun observeStatus() {
        viewModelScope.launch(dispatchers.main) {
            observeNotificationStatusUseCase().collect { status ->
                _uiState.update { current ->
                    val isRegistered = status is NotificationRegistrationStatus.Registered
                    val token = if (status is NotificationRegistrationStatus.Registered) {
                        status.token
                    } else {
                        null
                    }
                    current.copy(
                        registrationStatus = status,
                        isNotificationsEnabled = isRegistered,
                        currentToken = token,
                        isSyncing = status is NotificationRegistrationStatus.Registering
                    )
                }
            }
        }
    }

    fun updatePermissionState(hasPermission: Boolean) {
        _uiState.update { it.copy(hasSystemPermission = hasPermission) }
    }

    fun registerDeviceToken(explicitToken: String? = null) {
        _uiState.update { it.copy(isSyncing = true, userMessage = null, isError = false) }
        viewModelScope.launch(dispatchers.io) {
            val result = registerPushTokenUseCase(explicitToken)
            when (result) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            isNotificationsEnabled = true,
                            currentToken = result.data,
                            userMessage = null,
                            isError = false
                        )
                    }
                }
                is AppResult.Failure -> {
                    val resolvedError = ErrorMessageResolver.resolve(result.error)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            isNotificationsEnabled = false,
                            userMessage = resolvedError.fallbackMessage,
                            isError = true
                        )
                    }
                }
            }
        }
    }

    fun unregisterDeviceToken() {
        _uiState.update { it.copy(isSyncing = true, isNotificationsEnabled = false, userMessage = null) }
        viewModelScope.launch(dispatchers.io) {
            val result = unregisterPushTokenUseCase()
            val userMsg = if (result is AppResult.Failure) {
                ErrorMessageResolver.resolve(result.error).fallbackMessage
            } else null
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    isNotificationsEnabled = false,
                    currentToken = null,
                    userMessage = userMsg,
                    isError = result.isFailure
                )
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null, isError = false) }
    }
}
