package com.example.presentation.auth

import com.example.core.result.AppResult
import com.example.domain.model.auth.AuthProviderType
import com.example.domain.model.auth.AuthUser
import com.example.domain.model.auth.UserSession
import com.example.domain.repository.AuthRepository
import com.example.domain.usecase.auth.GetCurrentUserUseCase
import com.example.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.domain.usecase.auth.RefreshSessionUseCase
import com.example.domain.usecase.auth.ResetPasswordUseCase
import com.example.domain.usecase.auth.RestoreSessionUseCase
import com.example.domain.usecase.auth.SignInWithEmailUseCase
import com.example.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.domain.usecase.auth.SignOutUseCase
import com.example.domain.usecase.auth.SignUpWithEmailUseCase
import com.example.presentation.auth.state.AuthUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    private class FakeAuthRepository : AuthRepository {
        val authStateFlow = MutableStateFlow<AuthUser?>(null)
        var userInStorage: AuthUser? = null

        override fun observeAuthState(): Flow<AuthUser?> = authStateFlow
        override suspend fun getCurrentUser(): AuthUser? = authStateFlow.value
        override suspend fun getCurrentSession(): UserSession? = authStateFlow.value?.let {
            UserSession(accessToken = "access_token", user = it)
        }

        override suspend fun signUpWithEmail(email: String, password: String): AppResult<AuthUser> {
            val user = AuthUser(id = "id-1", email = email, provider = AuthProviderType.EMAIL)
            authStateFlow.value = user
            return AppResult.Success(user)
        }

        override suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser> {
            val user = AuthUser(id = "id-1", email = email, provider = AuthProviderType.EMAIL)
            authStateFlow.value = user
            return AppResult.Success(user)
        }

        override suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<AuthUser> {
            val user = AuthUser(id = "id-g", email = "google@enterprise.com", provider = AuthProviderType.GOOGLE)
            authStateFlow.value = user
            return AppResult.Success(user)
        }

        override suspend fun signOut(): AppResult<Unit> {
            authStateFlow.value = null
            return AppResult.Success(Unit)
        }

        override suspend fun resetPassword(email: String): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun refreshSession(): AppResult<AuthUser> {
            val refreshed = AuthUser(id = "id-refreshed", email = "refreshed@enterprise.com", provider = AuthProviderType.EMAIL)
            authStateFlow.value = refreshed
            return AppResult.Success(refreshed)
        }

        override suspend fun restoreSession(): AppResult<AuthUser?> {
            if (userInStorage != null) {
                authStateFlow.value = userInStorage
            }
            return AppResult.Success(userInStorage)
        }

        override suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser> {
            val updated = authStateFlow.value?.copy(avatarUrl = avatarUrl)
                ?: AuthUser(id = "avatar-user", email = "avatar@enterprise.com", provider = AuthProviderType.EMAIL, avatarUrl = avatarUrl)
            authStateFlow.value = updated
            return AppResult.Success(updated)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when restored session exists on startup, viewModel hydrates authenticated state`() = runTest {
        val persistedUser = AuthUser(id = "saved-123", email = "persisted@enterprise.com", provider = AuthProviderType.EMAIL)
        fakeRepo.userInStorage = persistedUser

        viewModel = AuthViewModel(
            observeAuthStateUseCase = ObserveAuthStateUseCase(fakeRepo),
            getCurrentUserUseCase = GetCurrentUserUseCase(fakeRepo),
            signInWithEmailUseCase = SignInWithEmailUseCase(fakeRepo),
            signUpWithEmailUseCase = SignUpWithEmailUseCase(fakeRepo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(fakeRepo),
            signOutUseCase = SignOutUseCase(fakeRepo),
            resetPasswordUseCase = ResetPasswordUseCase(fakeRepo),
            restoreSessionUseCase = RestoreSessionUseCase(fakeRepo),
            refreshSessionUseCase = RefreshSessionUseCase(fakeRepo)
        )

        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue(currentState is AuthUiState.Authenticated)
        assertEquals("persisted@enterprise.com", (currentState as AuthUiState.Authenticated).user.email)
    }

    @Test
    fun `when refreshSession is called, viewModel updates authenticated user`() = runTest {
        viewModel = AuthViewModel(
            observeAuthStateUseCase = ObserveAuthStateUseCase(fakeRepo),
            getCurrentUserUseCase = GetCurrentUserUseCase(fakeRepo),
            signInWithEmailUseCase = SignInWithEmailUseCase(fakeRepo),
            signUpWithEmailUseCase = SignUpWithEmailUseCase(fakeRepo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(fakeRepo),
            signOutUseCase = SignOutUseCase(fakeRepo),
            resetPasswordUseCase = ResetPasswordUseCase(fakeRepo),
            restoreSessionUseCase = RestoreSessionUseCase(fakeRepo),
            refreshSessionUseCase = RefreshSessionUseCase(fakeRepo)
        )
        advanceUntilIdle()

        viewModel.refreshSession()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Authenticated)
        assertEquals("refreshed@enterprise.com", (state as AuthUiState.Authenticated).user.email)
    }

    @Test
    fun `when updateAvatarUrl is called, viewModel updates authenticated user avatar`() = runTest {
        val initialUser = AuthUser(id = "u-avatar", email = "avatar@enterprise.com", provider = AuthProviderType.EMAIL)
        fakeRepo.authStateFlow.value = initialUser

        viewModel = AuthViewModel(
            observeAuthStateUseCase = ObserveAuthStateUseCase(fakeRepo),
            getCurrentUserUseCase = GetCurrentUserUseCase(fakeRepo),
            signInWithEmailUseCase = SignInWithEmailUseCase(fakeRepo),
            signUpWithEmailUseCase = SignUpWithEmailUseCase(fakeRepo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(fakeRepo),
            signOutUseCase = SignOutUseCase(fakeRepo),
            resetPasswordUseCase = ResetPasswordUseCase(fakeRepo)
        )
        advanceUntilIdle()

        viewModel.updateAvatarUrl("https://supabase.co/storage/v1/object/public/documents/avatars/avatar_u-avatar_123.jpg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Authenticated)
        assertEquals(
            "https://supabase.co/storage/v1/object/public/documents/avatars/avatar_u-avatar_123.jpg",
            (state as AuthUiState.Authenticated).user.avatarUrl
        )
    }
}
