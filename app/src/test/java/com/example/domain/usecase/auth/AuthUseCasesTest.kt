package com.example.domain.usecase.auth

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.auth.AuthProviderType
import com.example.domain.model.auth.AuthUser
import com.example.domain.model.auth.UserSession
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    private class FakeAuthRepository : AuthRepository {
        var currentUser: AuthUser? = null
        var shouldFail: Boolean = false

        override fun observeAuthState(): Flow<AuthUser?> = flowOf(currentUser)
        override suspend fun getCurrentUser(): AuthUser? = currentUser
        override suspend fun getCurrentSession(): UserSession? = currentUser?.let {
            UserSession(accessToken = "token", user = it)
        }

        override suspend fun signUpWithEmail(email: String, password: String): AppResult<AuthUser> {
            if (shouldFail) return AppResult.Failure(AppError.Auth("Sign up failed"))
            val user = AuthUser(id = "1", email = email, provider = AuthProviderType.EMAIL)
            currentUser = user
            return AppResult.Success(user)
        }

        override suspend fun signInWithEmail(email: String, password: String): AppResult<AuthUser> {
            if (shouldFail) return AppResult.Failure(AppError.Auth.invalidCredentials())
            val user = AuthUser(id = "1", email = email, provider = AuthProviderType.EMAIL)
            currentUser = user
            return AppResult.Success(user)
        }

        override suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<AuthUser> {
            if (shouldFail) return AppResult.Failure(AppError.Auth("Google sign in failed"))
            val user = AuthUser(id = "g-1", email = "google@gmail.com", provider = AuthProviderType.GOOGLE)
            currentUser = user
            return AppResult.Success(user)
        }

        override suspend fun signOut(): AppResult<Unit> {
            currentUser = null
            return AppResult.Success(Unit)
        }

        override suspend fun resetPassword(email: String): AppResult<Unit> {
            return if (shouldFail) AppResult.Failure(AppError.Auth("Reset failed")) else AppResult.Success(Unit)
        }

        override suspend fun refreshSession(): AppResult<AuthUser> {
            if (shouldFail) return AppResult.Failure(AppError.Auth(message = "Refresh token expired"))
            val user = currentUser ?: AuthUser(id = "refreshed-1", email = "refreshed@enterprise.com", provider = AuthProviderType.EMAIL)
            currentUser = user
            return AppResult.Success(user)
        }

        override suspend fun restoreSession(): AppResult<AuthUser?> {
            if (shouldFail) return AppResult.Failure(AppError.Auth("Restoration corrupted"))
            return AppResult.Success(currentUser)
        }

        override suspend fun updateUserAvatar(avatarUrl: String): AppResult<AuthUser> {
            val user = currentUser?.copy(avatarUrl = avatarUrl)
                ?: AuthUser(id = "1", email = "test@enterprise.com", provider = AuthProviderType.EMAIL, avatarUrl = avatarUrl)
            currentUser = user
            return AppResult.Success(user)
        }
    }

    @Test
    fun `RefreshSessionUseCase succeeds when repository successfully refreshes`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.currentUser = AuthUser("1", "user@enterprise.com")
        val useCase = RefreshSessionUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result is AppResult.Success)
        assertEquals("user@enterprise.com", (result as AppResult.Success).data.email)
    }

    @Test
    fun `RefreshSessionUseCase propagates failure on expired refresh token`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.shouldFail = true
        val useCase = RefreshSessionUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result is AppResult.Failure)
        assertEquals("Refresh token expired", (result as AppResult.Failure).error.message)
    }

    @Test
    fun `RestoreSessionUseCase recovers authenticated user on app restart`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.currentUser = AuthUser("saved-id", "saved@enterprise.com")
        val useCase = RestoreSessionUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result is AppResult.Success)
        assertEquals("saved@enterprise.com", (result as AppResult.Success).data?.email)
    }

    @Test
    fun `RestoreSessionUseCase returns null when no previous session exists`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.currentUser = null
        val useCase = RestoreSessionUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result is AppResult.Success)
        assertEquals(null, (result as AppResult.Success).data)
    }

    @Test
    fun `SignUpWithEmailUseCase rejects invalid email before calling repo`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        val useCase = SignUpWithEmailUseCase(fakeRepo)

        val result = useCase("invalid-email", "password123")
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Validation)
    }

    @Test
    fun `SignUpWithEmailUseCase succeeds with valid credentials`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        val useCase = SignUpWithEmailUseCase(fakeRepo)

        val result = useCase("test@enterprise.com", "securePassword123")
        assertTrue(result is AppResult.Success)
        assertEquals("test@enterprise.com", (result as AppResult.Success).data.email)
    }

    @Test
    fun `SignInWithGoogleUseCase validates token structure`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        val useCase = SignInWithGoogleUseCase(fakeRepo)

        val invalidResult = useCase("not-a-jwt")
        assertTrue(invalidResult is AppResult.Failure)

        val validResult = useCase("header.payload.sig")
        assertTrue(validResult is AppResult.Success)
    }

    @Test
    fun `SignOutUseCase clears user in repository`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        fakeRepo.currentUser = AuthUser("1", "test@test.com")
        val useCase = SignOutUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result is AppResult.Success)
        assertEquals(null, fakeRepo.currentUser)
    }
}
