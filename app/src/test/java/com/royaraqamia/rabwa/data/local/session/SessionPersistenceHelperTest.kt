package com.royaraqamia.rabwa.data.local.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SessionPersistenceHelperTest {

    private lateinit var context: Context
    private lateinit var persistenceHelper: SharedPreferencesSessionPersistenceHelper
    private lateinit var sessionManager: SharedPreferencesSessionManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear any old prefs
        context.getSharedPreferences("test_supabase_session_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        persistenceHelper = SharedPreferencesSessionPersistenceHelper(
            context = context,
            prefsName = "test_supabase_session_prefs"
        )
        sessionManager = SharedPreferencesSessionManager(persistenceHelper)
    }

    @Test
    fun `initial state has no persisted session`() {
        assertFalse(persistenceHelper.hasPersistedSession())
        assertNull(persistenceHelper.loadSession())
        assertNull(persistenceHelper.getSavedRefreshToken())
    }

    @Test
    fun `saveSession persists user session and tokens accurately`() = runBlocking {
        val testUser = UserInfo(
            id = "enterprise-user-123",
            email = "architect@enterprise.io",
            aud = "authenticated"
        )
        val testSession = UserSession(
            accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.enterprise.access",
            refreshToken = "enterprise_refresh_token_xyz_987",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = testUser
        )

        sessionManager.saveSession(testSession)

        assertTrue(persistenceHelper.hasPersistedSession())
        assertEquals("enterprise_refresh_token_xyz_987", persistenceHelper.getSavedRefreshToken())

        val loadedSession = sessionManager.loadSession()
        assertNotNull(loadedSession)
        assertEquals("enterprise-user-123", loadedSession?.user?.id)
        assertEquals("architect@enterprise.io", loadedSession?.user?.email)
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.enterprise.access", loadedSession?.accessToken)
        assertEquals("enterprise_refresh_token_xyz_987", loadedSession?.refreshToken)
    }

    @Test
    fun `deleteSession purges all stored session data`() = runBlocking {
        val testUser = UserInfo(
            id = "user-delete-test",
            email = "delete@enterprise.io",
            aud = "authenticated"
        )
        val testSession = UserSession(
            accessToken = "access_token_1",
            refreshToken = "refresh_token_1",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = testUser
        )

        sessionManager.saveSession(testSession)
        assertTrue(persistenceHelper.hasPersistedSession())

        sessionManager.deleteSession()
        assertFalse(persistenceHelper.hasPersistedSession())
        assertNull(sessionManager.loadSession())
        assertNull(persistenceHelper.getSavedRefreshToken())
    }
}
