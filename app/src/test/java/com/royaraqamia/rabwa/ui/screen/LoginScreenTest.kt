package com.royaraqamia.rabwa.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.auth.AuthProviderType
import com.royaraqamia.rabwa.domain.model.auth.AuthUser
import com.royaraqamia.rabwa.domain.model.auth.UserSession
import com.royaraqamia.rabwa.domain.repository.AuthRepository
import com.royaraqamia.rabwa.domain.usecase.auth.GetCurrentUserUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.ObserveAuthStateUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.ResetPasswordUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignInWithEmailUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignInWithGoogleUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignOutUseCase
import com.royaraqamia.rabwa.domain.usecase.auth.SignUpWithEmailUseCase
import com.royaraqamia.rabwa.presentation.auth.AuthViewModel
import com.royaraqamia.rabwa.presentation.auth.GoogleSignInHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class TestAuthRepository : AuthRepository {
        val userFlow = MutableStateFlow<AuthUser?>(null)
        var resetPasswordRequestedEmail: String? = null

        override fun observeAuthState(): Flow<AuthUser?> = userFlow
        override suspend fun getCurrentUser(): AuthUser? = userFlow.value
        override suspend fun getCurrentSession(): UserSession? = userFlow.value?.let {
            UserSession(accessToken = "token", user = it)
        }
        override suspend fun signUpWithEmail(email: String, password: String): AppResult<AuthUser> {
            val u = AuthUser(id = "1", email = email, provider = AuthProviderType.EMAIL)
            userFlow.value = u
            return AppResult.Success(u)
        }
        override suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser> {
            val u = AuthUser(id = "1", email = email, provider = AuthProviderType.EMAIL)
            userFlow.value = u
            return AppResult.Success(u)
        }
        override suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<AuthUser> {
            val u = AuthUser(id = "g-1", email = "google@user.com", provider = AuthProviderType.GOOGLE)
            userFlow.value = u
            return AppResult.Success(u)
        }
        override suspend fun signOut(): AppResult<Unit> {
            userFlow.value = null
            return AppResult.Success(Unit)
        }
        override suspend fun resetPassword(email: String): AppResult<Unit> {
            resetPasswordRequestedEmail = email
            return AppResult.Success(Unit)
        }
        override suspend fun refreshSession(): AppResult<AuthUser> {
            val u = userFlow.value ?: AuthUser(id = "1", email = "test@example.com", provider = AuthProviderType.EMAIL)
            return AppResult.Success(u)
        }
        override suspend fun restoreSession(): AppResult<AuthUser?> {
            return AppResult.Success(userFlow.value)
        }
        override suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser> {
            val u = userFlow.value?.copy(avatarUrl = avatarUrl)
                ?: AuthUser(id = "1", email = "test@example.com", provider = AuthProviderType.EMAIL, avatarUrl = avatarUrl)
            userFlow.value = u
            return AppResult.Success(u)
        }
    }

    private fun createAuthViewModel(repo: TestAuthRepository): AuthViewModel {
        return AuthViewModel(
            observeAuthStateUseCase = ObserveAuthStateUseCase(repo),
            getCurrentUserUseCase = GetCurrentUserUseCase(repo),
            signInWithEmailUseCase = SignInWithEmailUseCase(repo),
            signUpWithEmailUseCase = SignUpWithEmailUseCase(repo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(repo),
            signOutUseCase = SignOutUseCase(repo),
            resetPasswordUseCase = ResetPasswordUseCase(repo)
        )
    }

    @Test
    fun `login screen displays all auth controls and back button`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)
        var backPressed = false

        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    authViewModel = viewModel,
                    onNavigateBack = { backPressed = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("login_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_brand_logo").assertDoesNotExist()
        composeTestRule.onNodeWithTag("auth_mode_tabs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_email_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_password_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("auth_forgot_password_button").assertExists()
        composeTestRule.onNodeWithTag("auth_submit_button").assertExists()
        composeTestRule.onNodeWithTag("auth_google_button").assertExists()
        composeTestRule.onNodeWithTag("auth_continue_guest_button").assertExists()

        // Test back button
        composeTestRule.onNodeWithTag("login_back_button").performClick()
        assertTrue(backPressed)
    }

    @Test
    fun `switching to sign up tab updates button state and hides forgot password`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)

        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    authViewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("tab_sign_up").performClick()
        composeTestRule.onNodeWithTag("auth_forgot_password_button").assertDoesNotExist()
    }

    @Test
    fun `forgot password dialog allows requesting reset link`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)

        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    authViewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("auth_forgot_password_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("forgot_password_dialog").assertIsDisplayed()

        composeTestRule.onNodeWithTag("forgot_password_email_input").performTextInput("architect@test.com")
        composeTestRule.onNodeWithTag("forgot_password_submit_button").performClick()

        composeTestRule.waitForIdle()
        assertEquals("architect@test.com", repo.resetPasswordRequestedEmail)
    }

    @Test
    fun `reporting no google account error shows add account and switch to email actions`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)
        
        val fakeGoogleHelper = object : GoogleSignInHelper(androidx.test.core.app.ApplicationProvider.getApplicationContext()) {
            override suspend fun launchGoogleSignIn(serverClientId: String?): AppResult<GoogleSignInPayload> {
                return AppResult.Failure(AppError.Auth(code = ERROR_CODE_NO_ACCOUNT, message = "No account found"))
            }
        }

        composeTestRule.setContent {
            MaterialTheme {
                LoginScreen(
                    authViewModel = viewModel,
                    onNavigateBack = {},
                    googleSignInHelper = fakeGoogleHelper
                )
            }
        }

        // Initially banner is not displayed
        composeTestRule.onNodeWithTag("auth_banner_add_google_account_button").assertDoesNotExist()

        // Trigger Google Sign-In with missing config or simulated failure
        composeTestRule.onNodeWithTag("auth_google_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Banner appears
        composeTestRule.onNodeWithTag("auth_feedback_banner").assertIsDisplayed()
    }
}

