package com.example.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.core.dispatcher.DefaultDispatchers
import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.LayerHealth
import com.example.domain.model.NetworkStatus
import com.example.domain.model.SyncStatus
import com.example.domain.model.SyncSummary
import com.example.domain.model.auth.AuthProviderType
import com.example.domain.model.auth.AuthUser
import com.example.domain.model.auth.UserSession
import com.example.domain.network.ConnectivityMonitor
import com.example.domain.repository.ArchitectureRepository
import com.example.domain.repository.AuthRepository
import com.example.domain.usecase.AddArchitectureRecordUseCase
import com.example.domain.usecase.ClearRecordsUseCase
import com.example.domain.usecase.GetLayerHealthUseCase
import com.example.domain.usecase.ObserveNetworkStatusUseCase
import com.example.domain.usecase.ObserveRecordsUseCase
import com.example.domain.usecase.ObserveSyncSummaryUseCase
import com.example.domain.usecase.SyncOfflineRecordsUseCase
import com.example.domain.usecase.auth.GetCurrentUserUseCase
import com.example.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.domain.usecase.auth.ResetPasswordUseCase
import com.example.domain.usecase.auth.SignInWithEmailUseCase
import com.example.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.domain.usecase.auth.SignOutUseCase
import com.example.domain.usecase.auth.SignUpWithEmailUseCase
import com.example.presentation.ArchitectureViewModel
import com.example.presentation.auth.AuthViewModel
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainAppScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeArchitectureRepository : ArchitectureRepository {
        private val recordsFlow = MutableStateFlow<List<ArchitectureRecord>>(emptyList())
        private val syncFlow = MutableStateFlow(SyncSummary(0, 0, 0))

        override suspend fun getLayerHealth(): AppResult<List<LayerHealth>> =
            AppResult.Success(emptyList())

        override suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord> =
            AppResult.Success(ArchitectureRecord("1", sanitizedTitle, System.currentTimeMillis(), SyncStatus.SYNCED))

        override suspend fun clearRecords(): AppResult<Unit> = AppResult.Success(Unit)
        override fun observeRecords(): Flow<List<ArchitectureRecord>> = recordsFlow.asStateFlow()
        override fun observeSyncSummary(): Flow<SyncSummary> = syncFlow.asStateFlow()
        override suspend fun synchronizePendingRecords(): AppResult<Int> = AppResult.Success(0)
    }

    private class FakeConnectivityMonitor : ConnectivityMonitor {
        private val statusFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)
        override val networkStatus: Flow<NetworkStatus> = statusFlow.asStateFlow()
        override val isCurrentlyConnected: Boolean = true
    }

    private class FakeAuthRepository : AuthRepository {
        val userFlow = MutableStateFlow<AuthUser?>(null)
        override fun observeAuthState(): Flow<AuthUser?> = userFlow.asStateFlow()
        override suspend fun getCurrentUser(): AuthUser? = userFlow.value
        override suspend fun getCurrentSession(): UserSession? = null
        override suspend fun signUpWithEmail(email: String, password: String): AppResult<AuthUser> =
            AppResult.Success(AuthUser("1", email, provider = AuthProviderType.EMAIL))
        override suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser> =
            AppResult.Success(AuthUser("1", email, provider = AuthProviderType.EMAIL))
        override suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<AuthUser> =
            AppResult.Success(AuthUser("g-1", "g@test.com", provider = AuthProviderType.GOOGLE))
        override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun resetPassword(email: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun refreshSession(): AppResult<AuthUser> =
            AppResult.Success(AuthUser("1", "test@test.com", provider = AuthProviderType.EMAIL))
        override suspend fun restoreSession(): AppResult<AuthUser?> =
            AppResult.Success(userFlow.value)
        override suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser> {
            val u = userFlow.value?.copy(avatarUrl = avatarUrl)
                ?: AuthUser(id = "1", email = "test@test.com", provider = AuthProviderType.EMAIL, avatarUrl = avatarUrl)
            userFlow.value = u
            return AppResult.Success(u)
        }
    }

    @Test
    fun `user can use app freely without login and switch to profile tab`() {
        val archRepo = FakeArchitectureRepository()
        val connectivity = FakeConnectivityMonitor()
        val authRepo = FakeAuthRepository()
        val dispatchers = DefaultDispatchers()

        val archViewModel = ArchitectureViewModel(
            getLayerHealthUseCase = GetLayerHealthUseCase(archRepo),
            addArchitectureRecordUseCase = AddArchitectureRecordUseCase(archRepo),
            observeRecordsUseCase = ObserveRecordsUseCase(archRepo),
            clearRecordsUseCase = ClearRecordsUseCase(archRepo),
            observeNetworkStatusUseCase = ObserveNetworkStatusUseCase(connectivity),
            syncOfflineRecordsUseCase = SyncOfflineRecordsUseCase(archRepo),
            observeSyncSummaryUseCase = ObserveSyncSummaryUseCase(archRepo),
            dispatchers = dispatchers
        )

        val authViewModel = AuthViewModel(
            observeAuthStateUseCase = ObserveAuthStateUseCase(authRepo),
            getCurrentUserUseCase = GetCurrentUserUseCase(authRepo),
            signInWithEmailUseCase = SignInWithEmailUseCase(authRepo),
            signUpWithEmailUseCase = SignUpWithEmailUseCase(authRepo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepo),
            signOutUseCase = SignOutUseCase(authRepo),
            resetPasswordUseCase = ResetPasswordUseCase(authRepo)
        )

        composeTestRule.setContent {
            MaterialTheme {
                MainAppScreen(
                    architectureViewModel = archViewModel,
                    authViewModel = authViewModel,
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeChange = {}
                )
            }
        }

        // Verify Home screen is immediately accessible without login
        composeTestRule.onNodeWithTag("architecture_dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_bottom_navigation_bar").assertIsDisplayed()

        // Switch to Profile Tab
        composeTestRule.onNodeWithTag("nav_profile_tab").performClick()
        composeTestRule.waitForIdle()

        // Profile Screen should display guest status and login button
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("guest_profile_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_navigation_button").assertIsDisplayed()

        // Click Login Button -> Navigates to Login Screen
        composeTestRule.onNodeWithTag("login_navigation_button").performClick()
        composeTestRule.waitForIdle()

        // Login Screen should be displayed with back button
        composeTestRule.onNodeWithTag("login_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_back_button").assertIsDisplayed()

        // Click back button -> returns to Home
        composeTestRule.onNodeWithTag("login_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("architecture_dashboard_screen").assertIsDisplayed()
    }
}
