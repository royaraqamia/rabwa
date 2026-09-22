package com.royaraqamia.rabwa.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royaraqamia.rabwa.core.config.SupabaseConfig
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.usecase.auth.GetCurrentUserUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.ObserveAuthStateUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.RefreshSessionUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.ResetPasswordUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.RestoreSessionUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignInWithEmailUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignInWithGoogleUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignOutUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignUpWithEmailUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.UpdateUserAvatarUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.RegisterPushTokenUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.UnregisterPushTokenUseCase
import com.royaraqamia.rabwa.presentation.auth.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase? = null,
    private val refreshSessionUseCase: RefreshSessionUseCase? = null,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase? = null,
    private val registerPushTokenUseCase: RegisterPushTokenUseCase? = null,
    private val unregisterPushTokenUseCase: UnregisterPushTokenUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isAvatarUploading = MutableStateFlow(false)
    val isAvatarUploading: StateFlow<Boolean> = _isAvatarUploading.asStateFlow()

    val isSupabaseConfigured: Boolean = SupabaseConfig.loadFromBuildConfig().isConfigured

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            // 1. Attempt to restore persisted session on startup
            restoreSessionUseCase?.invoke()

            // 2. Check current active session user
            val initialUser = getCurrentUserUseCase()
            if (initialUser != null) {
                _uiState.value = AuthUiState.Authenticated(initialUser)
                launch {
                    try { registerPushTokenUseCase?.invoke() } catch (_: Exception) {}
                }
            } else {
                _uiState.value = AuthUiState.Unauthenticated
            }

            // 3. Continuously observe session status stream
            observeAuthStateUseCase().collect { user ->
                _uiState.value = if (user != null) {
                    launch {
                        try { registerPushTokenUseCase?.invoke() } catch (_: Exception) {}
                    }
                    AuthUiState.Authenticated(user)
                } else {
                    AuthUiState.Unauthenticated
                }
            }
        }
    }

    fun refreshSession() {
        viewModelScope.launch {
            val refreshUseCase = refreshSessionUseCase ?: return@launch
            when (val result = refreshUseCase()) {
                is AppResult.Success -> {
                    _uiState.value = AuthUiState.Authenticated(result.data)
                }
                is AppResult.Failure -> {
                    // Non-fatal if offline, let status observer handle state transitions
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _errorMessage.value = null
            when (val result = signInWithEmailUseCase(email, password)) {
                is AppResult.Success -> {
                    _uiState.value = AuthUiState.Authenticated(result.data)
                    _actionMessage.value = "تم تسجيل الدخول بنجاح"
                    registerPushTokenUseCase?.invoke()
                }
                is AppResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.error)
                    _errorMessage.value = result.error.message
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _errorMessage.value = null
            when (val result = signUpWithEmailUseCase(email, password)) {
                is AppResult.Success -> {
                    _uiState.value = AuthUiState.Authenticated(result.data)
                    _actionMessage.value = "تم إنشاء الحساب بنجاح"
                    registerPushTokenUseCase?.invoke()
                }
                is AppResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.error)
                    _errorMessage.value = result.error.message
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, rawNonce: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _errorMessage.value = null
            when (val result = signInWithGoogleUseCase(idToken, rawNonce)) {
                is AppResult.Success -> {
                    _uiState.value = AuthUiState.Authenticated(result.data)
                    _actionMessage.value = "تم تسجيل الدخول بواسطة Google بنجاح"
                    registerPushTokenUseCase?.invoke()
                }
                is AppResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.error)
                    _errorMessage.value = result.error.message
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { unregisterPushTokenUseCase?.invoke() }
            when (val result = signOutUseCase()) {
                is AppResult.Success -> {
                    _uiState.value = AuthUiState.Unauthenticated
                    _actionMessage.value = "تم تسجيل الخروج"
                }
                is AppResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.error)
                    _errorMessage.value = result.error.message
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            when (val result = resetPasswordUseCase(email)) {
                is AppResult.Success -> {
                    _actionMessage.value = "تم إرسال رابط استعادة كلمة المرور إلى بريدك الإلكتروني"
                }
                is AppResult.Failure -> {
                    _errorMessage.value = result.error.message
                }
            }
        }
    }

    fun reportAuthError(error: AppError) {
        _uiState.value = AuthUiState.Error(error)
        _errorMessage.value = error.message
    }

    fun updateAvatarFromBytes(imageBytes: ByteArray, mimeType: String = "image/jpeg") {
        val currentState = _uiState.value
        val userId = if (currentState is AuthUiState.Authenticated) currentState.user.id else "guest"

        viewModelScope.launch {
            _isAvatarUploading.value = true
            _errorMessage.value = null

            val useCase = updateUserAvatarUseCase
            if (useCase != null) {
                when (val result = useCase.uploadAndSaveAvatar(userId, imageBytes, mimeType)) {
                    is AppResult.Success -> {
                        _uiState.value = AuthUiState.Authenticated(result.data)
                        _actionMessage.value = "تم حفظ وتحديث الصورة الشخصية بنجاح"
                    }
                    is AppResult.Failure -> {
                        if (currentState is AuthUiState.Authenticated) {
                            val base64Data = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                            val updatedUser = currentState.user.copy(avatarUrl = base64Data)
                            _uiState.value = AuthUiState.Authenticated(updatedUser)
                            _actionMessage.value = "تم تحديث الصورة الشخصية محلياً"
                        } else {
                            _errorMessage.value = result.error.message
                        }
                    }
                }
            } else {
                if (currentState is AuthUiState.Authenticated) {
                    val base64Data = "data:image/jpeg;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                    _uiState.value = AuthUiState.Authenticated(currentState.user.copy(avatarUrl = base64Data))
                }
                _actionMessage.value = "تم تحديث الصورة الشخصية محلياً"
            }
            _isAvatarUploading.value = false
        }
    }

    fun updateAvatarUrl(avatarUrl: String) {
        viewModelScope.launch {
            _isAvatarUploading.value = true
            _errorMessage.value = null

            val useCase = updateUserAvatarUseCase
            if (useCase != null) {
                when (val result = useCase.saveAvatarUrl(avatarUrl)) {
                    is AppResult.Success -> {
                        _uiState.value = AuthUiState.Authenticated(result.data)
                        _actionMessage.value = "تم حفظ رابط الصورة الشخصية في حسابك بنجاح"
                    }
                    is AppResult.Failure -> {
                        _errorMessage.value = result.error.message
                    }
                }
            } else {
                val currentState = _uiState.value
                if (currentState is AuthUiState.Authenticated) {
                    _uiState.value = AuthUiState.Authenticated(currentState.user.copy(avatarUrl = avatarUrl))
                }
                _actionMessage.value = "تم تحديث رابط الصورة الشخصية"
            }
            _isAvatarUploading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _actionMessage.value = null
    }
}
