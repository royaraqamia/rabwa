package com.royaraqamia.rabwa.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.royaraqamia.rabwa.domain.model.notification.NotificationRegistrationStatus
import com.royaraqamia.rabwa.domain.model.notification.PushNotificationPayload
import com.royaraqamia.rabwa.domain.repository.NotificationRepository
import com.royaraqamia.rabwa.presentation.notification.NotificationViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertNull
import com.royaraqamia.rabwa.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class TestAuthRepository : AuthRepository {
        val userFlow = MutableStateFlow<AuthUser?>(null)

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
        override suspend fun resetPassword(email: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun refreshSession(): AppResult<AuthUser> =
            AppResult.Success(AuthUser("1", "profile@user.com", provider = AuthProviderType.EMAIL))
        override suspend fun restoreSession(): AppResult<AuthUser?> =
            AppResult.Success(userFlow.value)
        override suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser> {
            val updated = userFlow.value?.copy(avatarUrl = avatarUrl)
                ?: AuthUser(id = "1", email = "profile@user.com", provider = AuthProviderType.EMAIL, avatarUrl = avatarUrl)
            userFlow.value = updated
            return AppResult.Success(updated)
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
    fun `when unauthenticated guest profile displays guest card and login navigation button`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)
        var navigatedToLogin = false

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = viewModel,
                    onNavigateToLogin = { navigatedToLogin = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("guest_profile_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_navigation_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("login_navigation_button").performClick()
        assertTrue(navigatedToLogin)
    }

    @Test
    fun `when authenticated profile displays email and logout button`() {
        val repo = TestAuthRepository()
        repo.userFlow.value = AuthUser(id = "u-42", email = "architect@enterprise.com", provider = AuthProviderType.EMAIL)
        val viewModel = createAuthViewModel(repo)
        var navigatedToLogin = false

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = viewModel,
                    onNavigateToLogin = { navigatedToLogin = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("authenticated_profile_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_email_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_email_text").assertTextEquals("architect@enterprise.com")
        composeTestRule.onNodeWithTag("logout_button").performScrollTo().assertIsDisplayed()

        // Clicking logout calls signOut and triggers navigation to login
        composeTestRule.onNodeWithTag("logout_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(navigatedToLogin)
        composeTestRule.onNodeWithTag("guest_profile_card").assertIsDisplayed()
    }

    @Test
    fun `theme switch toggles dark and light mode`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)
        var updatedThemeMode: ThemeMode? = null

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = viewModel,
                    themeMode = ThemeMode.LIGHT,
                    onThemeModeChange = { updatedThemeMode = it },
                    onNavigateToLogin = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_switch").assertIsDisplayed()

        composeTestRule.onNodeWithTag("theme_switch").performClick()
        assertEquals(ThemeMode.DARK, updatedThemeMode)
    }

    @Test
    fun `theme switch toggles from DARK to LIGHT mode`() {
        val repo = TestAuthRepository()
        val viewModel = createAuthViewModel(repo)
        var updatedThemeMode: ThemeMode? = null

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = viewModel,
                    themeMode = ThemeMode.DARK,
                    onThemeModeChange = { updatedThemeMode = it },
                    onNavigateToLogin = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("theme_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_switch").assertIsDisplayed()

        composeTestRule.onNodeWithTag("theme_switch").performClick()
        assertEquals(ThemeMode.LIGHT, updatedThemeMode)
    }

    private class TestNotificationRepository : NotificationRepository {
        val statusFlow = MutableStateFlow<NotificationRegistrationStatus>(NotificationRegistrationStatus.Unregistered)
        override val registrationStatus: StateFlow<NotificationRegistrationStatus> = statusFlow.asStateFlow()

        val foregroundFlow = MutableSharedFlow<PushNotificationPayload>()
        override val foregroundNotifications: SharedFlow<PushNotificationPayload> = foregroundFlow.asSharedFlow()
        override val hasActiveObservers: Boolean = false

        override suspend fun registerCurrentDeviceToken(token: String): AppResult<Unit> {
            statusFlow.value = NotificationRegistrationStatus.Registered(token)
            return AppResult.Success(Unit)
        }

        override suspend fun unregisterCurrentDeviceToken(): AppResult<Unit> {
            statusFlow.value = NotificationRegistrationStatus.Unregistered
            return AppResult.Success(Unit)
        }

        override suspend fun getFcmToken(): AppResult<String> = AppResult.Success("test_device_fcm_token")

        override suspend fun fetchAndRegisterToken(): AppResult<String> {
            statusFlow.value = NotificationRegistrationStatus.Registered("test_device_fcm_token")
            return AppResult.Success("test_device_fcm_token")
        }

        override suspend fun syncPendingToken(): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun dispatchForegroundNotification(payload: PushNotificationPayload) {
            foregroundFlow.emit(payload)
        }
    }

    @Test
    fun `notification toggle switch transitions from ON to OFF and has no green message banner`() {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        org.robolectric.Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

        val authRepo = TestAuthRepository()
        val authViewModel = createAuthViewModel(authRepo)
        val notifRepo = TestNotificationRepository()
        val notifViewModel = NotificationViewModel(
            registerPushTokenUseCase = com.royaraqamia.rabwa.domain.usecase.notification.RegisterPushTokenUseCase(notifRepo),
            unregisterPushTokenUseCase = com.royaraqamia.rabwa.domain.usecase.notification.UnregisterPushTokenUseCase(notifRepo),
            observeNotificationStatusUseCase = com.royaraqamia.rabwa.domain.usecase.notification.ObserveNotificationStatusUseCase(notifRepo),
            dispatchers = com.royaraqamia.rabwa.testutil.TestCoroutineDispatchers()
        )

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = authViewModel,
                    notificationViewModel = notifViewModel,
                    onNavigateToLogin = {}
                )
            }
        }

        // Notification settings card displayed
        composeTestRule.onNodeWithTag("notification_settings_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notification_switch").assertIsDisplayed()

        // Turn notifications ON
        composeTestRule.onNodeWithTag("notification_switch").performClick()
        composeTestRule.waitForIdle()

        assertTrue(notifViewModel.uiState.value.isNotificationsEnabled)
        assertNull(notifViewModel.uiState.value.userMessage) // No green message

        // Turn notifications OFF
        composeTestRule.onNodeWithTag("notification_switch").performClick()
        composeTestRule.waitForIdle()

        org.junit.Assert.assertFalse(notifViewModel.uiState.value.isNotificationsEnabled)
        assertNull(notifViewModel.uiState.value.userMessage) // No green message
    }

    @Test
    fun `profile screen top bar contains QR code button and footer shows version 1_0_0 only`() {
        val authRepo = TestAuthRepository()
        val authViewModel = createAuthViewModel(authRepo)
        val qrScannerViewModel = com.royaraqamia.rabwa.presentation.qr.QrScannerViewModel(
            processQrScanUseCase = com.royaraqamia.rabwa.domain.usecase.qr.ProcessQrScanUseCase(),
            dispatchers = com.royaraqamia.rabwa.testutil.TestCoroutineDispatchers()
        )

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = authViewModel,
                    qrScannerViewModel = qrScannerViewModel,
                    onNavigateToLogin = {}
                )
            }
        }

        // Verify QR scanner button is displayed in TopAppBar
        composeTestRule.onNodeWithTag("profile_app_bar_qr_scan_button").assertIsDisplayed()

        // Verify About card and logo are removed, only version 1.0.0 text is shown
        composeTestRule.onNodeWithTag("about_app_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("about_app_logo").assertDoesNotExist()
        composeTestRule.onNodeWithTag("app_version_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_version_text").assertTextEquals("1.0.0")

        // Clicking QR scan button opens scanner modal
        composeTestRule.onNodeWithTag("profile_app_bar_qr_scan_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(qrScannerViewModel.uiState.value.isScannerOpen)
    }

    @Test
    fun `profile screen displays profile avatar component and allows opening option dialog`() {
        val repo = TestAuthRepository()
        repo.userFlow.value = AuthUser(id = "u-42", email = "architect@enterprise.com", provider = AuthProviderType.EMAIL)
        val viewModel = createAuthViewModel(repo)

        composeTestRule.setContent {
            MaterialTheme {
                ProfileScreen(
                    authViewModel = viewModel,
                    onNavigateToLogin = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("change_avatar_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("avatar_camera_badge").assertIsDisplayed()

        composeTestRule.onNodeWithTag("change_avatar_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("avatar_picker_dialog", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("pick_placeholder_option", useUnmergedTree = true).assertExists()
    }
}
