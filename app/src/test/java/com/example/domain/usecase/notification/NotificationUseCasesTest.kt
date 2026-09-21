package com.example.domain.usecase.notification

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.notification.NotificationRegistrationStatus
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationUseCasesTest {

    private class FakeNotificationRepository : NotificationRepository {
        val statusFlow = MutableStateFlow<NotificationRegistrationStatus>(NotificationRegistrationStatus.Unregistered)
        override val registrationStatus: StateFlow<NotificationRegistrationStatus> = statusFlow.asStateFlow()

        var registeredToken: String? = null
        var shouldFail: Boolean = false

        override suspend fun registerCurrentDeviceToken(token: String): AppResult<Unit> {
            return if (shouldFail) {
                AppResult.Failure(AppError.RemoteDatabase(message = "Simulated Supabase registration failure"))
            } else {
                registeredToken = token
                statusFlow.value = NotificationRegistrationStatus.Registered(token)
                AppResult.Success(Unit)
            }
        }

        override suspend fun unregisterCurrentDeviceToken(): AppResult<Unit> {
            registeredToken = null
            statusFlow.value = NotificationRegistrationStatus.Unregistered
            return AppResult.Success(Unit)
        }

        override suspend fun getFcmToken(): AppResult<String> {
            return if (shouldFail) {
                AppResult.Failure(AppError.RemoteDatabase(message = "Simulated FCM token failure"))
            } else {
                AppResult.Success("fake_fcm_token_12345")
            }
        }

        override suspend fun fetchAndRegisterToken(): AppResult<String> {
            return when (val tokenRes = getFcmToken()) {
                is AppResult.Success -> {
                    val regRes = registerCurrentDeviceToken(tokenRes.data)
                    if (regRes is AppResult.Success) {
                        AppResult.Success(tokenRes.data)
                    } else {
                        AppResult.Failure((regRes as AppResult.Failure).error)
                    }
                }
                is AppResult.Failure -> tokenRes
            }
        }
    }

    @Test
    fun registerPushTokenUseCase_withExplicitToken_registersSuccessfully() = runTest {
        val repo = FakeNotificationRepository()
        val useCase = RegisterPushTokenUseCase(repo)

        val result = useCase("custom_token_abc")

        assertTrue(result.isSuccess)
        assertEquals("custom_token_abc", (result as AppResult.Success).data)
        assertEquals("custom_token_abc", repo.registeredToken)
    }

    @Test
    fun registerPushTokenUseCase_withoutToken_fetchesAndRegisters() = runTest {
        val repo = FakeNotificationRepository()
        val useCase = RegisterPushTokenUseCase(repo)

        val result = useCase(null)

        assertTrue(result.isSuccess)
        assertEquals("fake_fcm_token_12345", (result as AppResult.Success).data)
        assertEquals("fake_fcm_token_12345", repo.registeredToken)
    }

    @Test
    fun registerPushTokenUseCase_failure_returnsAppError() = runTest {
        val repo = FakeNotificationRepository().apply { shouldFail = true }
        val useCase = RegisterPushTokenUseCase(repo)

        val result = useCase("test_token")

        assertTrue(result.isFailure)
        assertEquals("Simulated Supabase registration failure", result.errorOrNull()?.message)
    }

    @Test
    fun unregisterPushTokenUseCase_resetsState() = runTest {
        val repo = FakeNotificationRepository()
        repo.statusFlow.value = NotificationRegistrationStatus.Registered("token_xyz")
        val useCase = UnregisterPushTokenUseCase(repo)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertNull(repo.registeredToken)
        assertEquals(NotificationRegistrationStatus.Unregistered, repo.statusFlow.value)
    }

    @Test
    fun handleIncomingNotificationUseCase_sanitizesAndValidatesPayload() {
        val useCase = HandleIncomingNotificationUseCase()

        // Valid payload
        val valid = useCase(
            rawTitle = "   New Task Assigned   ",
            rawBody = "  Please review architectural design. ",
            rawData = mapOf("channel_id" to "tasks", "task_id" to "101")
        )

        assertNotNull(valid)
        assertEquals("New Task Assigned", valid?.title)
        assertEquals("Please review architectural design.", valid?.body)
        assertEquals("tasks", valid?.channelId)
        assertEquals("101", valid?.data?.get("task_id"))

        // Completely blank payload should be rejected (Zero Trust)
        val invalid = useCase(
            rawTitle = "   ",
            rawBody = null,
            rawData = emptyMap()
        )
        assertNull(invalid)
    }
}
