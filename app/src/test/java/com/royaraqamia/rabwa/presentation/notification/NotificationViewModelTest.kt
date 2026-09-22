package com.royaraqamia.rabwa.presentation.notification

import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.notification.NotificationRegistrationStatus
import com.royaraqamia.rabwa.domain.model.notification.PushNotificationPayload
import com.royaraqamia.rabwa.domain.repository.NotificationRepository
import com.royaraqamia.rabwa.domain.usecase.notification.ObserveNotificationStatusUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.RegisterPushTokenUseCase
import com.royaraqamia.rabwa.domain.usecase.notification.UnregisterPushTokenUseCase
import com.royaraqamia.rabwa.testutil.TestCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private class FakeNotificationRepository : NotificationRepository {
        val statusFlow = MutableStateFlow<NotificationRegistrationStatus>(NotificationRegistrationStatus.Unregistered)
        override val registrationStatus: StateFlow<NotificationRegistrationStatus> = statusFlow.asStateFlow()

        val foregroundFlow = MutableSharedFlow<PushNotificationPayload>()
        override val foregroundNotifications: SharedFlow<PushNotificationPayload> = foregroundFlow.asSharedFlow()
        override val hasActiveObservers: Boolean = false

        var shouldFail = false

        override suspend fun registerCurrentDeviceToken(token: String): AppResult<Unit> {
            return if (shouldFail) {
                AppResult.Failure(AppError.RemoteDatabase(message = "Network error registering token"))
            } else {
                statusFlow.value = NotificationRegistrationStatus.Registered(token)
                AppResult.Success(Unit)
            }
        }

        override suspend fun unregisterCurrentDeviceToken(): AppResult<Unit> {
            statusFlow.value = NotificationRegistrationStatus.Unregistered
            return AppResult.Success(Unit)
        }

        override suspend fun getFcmToken(): AppResult<String> {
            return AppResult.Success("fcm_token_test_abc")
        }

        override suspend fun fetchAndRegisterToken(): AppResult<String> {
            return if (shouldFail) {
                AppResult.Failure(AppError.RemoteDatabase(message = "Network error registering token"))
            } else {
                statusFlow.value = NotificationRegistrationStatus.Registered("fcm_token_test_abc")
                AppResult.Success("fcm_token_test_abc")
            }
        }

        override suspend fun syncPendingToken(): AppResult<Unit> {
            return if (shouldFail) {
                AppResult.Failure(AppError.RemoteDatabase(message = "Network error syncing token"))
            } else {
                AppResult.Success(Unit)
            }
        }

        override suspend fun dispatchForegroundNotification(payload: PushNotificationPayload) {
            foregroundFlow.emit(payload)
        }
    }

    @Test
    fun registerDeviceToken_success_updatesUiState() = runTest {
        val dispatchers = TestCoroutineDispatchers()
        val repo = FakeNotificationRepository()
        val viewModel = NotificationViewModel(
            registerPushTokenUseCase = RegisterPushTokenUseCase(repo),
            unregisterPushTokenUseCase = UnregisterPushTokenUseCase(repo),
            observeNotificationStatusUseCase = ObserveNotificationStatusUseCase(repo),
            dispatchers = dispatchers
        )

        viewModel.registerDeviceToken("test_token_999")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSyncing)
        assertTrue(state.isNotificationsEnabled)
        assertEquals("test_token_999", state.currentToken)
        assertFalse(state.isError)
        assertNull(state.userMessage) // Green success message is removed
    }

    @Test
    fun registerDeviceToken_failure_setsErrorMessage() = runTest {
        val dispatchers = TestCoroutineDispatchers()
        val repo = FakeNotificationRepository().apply { shouldFail = true }
        val viewModel = NotificationViewModel(
            registerPushTokenUseCase = RegisterPushTokenUseCase(repo),
            unregisterPushTokenUseCase = UnregisterPushTokenUseCase(repo),
            observeNotificationStatusUseCase = ObserveNotificationStatusUseCase(repo),
            dispatchers = dispatchers
        )

        viewModel.registerDeviceToken("fail_token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSyncing)
        assertFalse(state.isNotificationsEnabled)
        assertTrue(state.isError)
        assertEquals("تعذر الاتصال بقاعدة البيانات السحابية (Supabase).", state.userMessage)
    }

    @Test
    fun unregisterDeviceToken_clearsToken() = runTest {
        val dispatchers = TestCoroutineDispatchers()
        val repo = FakeNotificationRepository()
        val viewModel = NotificationViewModel(
            registerPushTokenUseCase = RegisterPushTokenUseCase(repo),
            unregisterPushTokenUseCase = UnregisterPushTokenUseCase(repo),
            observeNotificationStatusUseCase = ObserveNotificationStatusUseCase(repo),
            dispatchers = dispatchers
        )

        viewModel.registerDeviceToken("initial_token")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isNotificationsEnabled)

        viewModel.unregisterDeviceToken()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.currentToken)
        assertFalse(state.isNotificationsEnabled)
        assertNull(state.userMessage) // Green message removed
        assertEquals(NotificationRegistrationStatus.Unregistered, state.registrationStatus)
    }

    @Test
    fun updatePermissionState_updatesFlag() {
        val dispatchers = TestCoroutineDispatchers()
        val repo = FakeNotificationRepository()
        val viewModel = NotificationViewModel(
            registerPushTokenUseCase = RegisterPushTokenUseCase(repo),
            unregisterPushTokenUseCase = UnregisterPushTokenUseCase(repo),
            observeNotificationStatusUseCase = ObserveNotificationStatusUseCase(repo),
            dispatchers = dispatchers
        )

        viewModel.updatePermissionState(true)
        assertTrue(viewModel.uiState.value.hasSystemPermission)

        viewModel.updatePermissionState(false)
        assertFalse(viewModel.uiState.value.hasSystemPermission)
    }
}
