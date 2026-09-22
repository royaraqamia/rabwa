package com.royaraqamia.rabwa.data.local.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.royaraqamia.rabwa.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SharedPreferencesThemePreferencesManagerTest {

    private lateinit var context: Context
    private lateinit var manager: SharedPreferencesThemePreferencesManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs before test
        context.getSharedPreferences("test_theme_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        manager = SharedPreferencesThemePreferencesManager(context, "test_theme_prefs")
    }

    @Test
    fun `default theme mode is SYSTEM when no preference is saved`() {
        assertEquals(ThemeMode.SYSTEM, manager.getThemeMode())
        assertEquals(ThemeMode.SYSTEM, manager.themeModeState.value)
    }

    @Test
    fun `saving theme mode persists value across manager instances`() {
        manager.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, manager.getThemeMode())
        assertEquals(ThemeMode.DARK, manager.themeModeState.value)

        // Create new manager instance referencing same prefs
        val newManager = SharedPreferencesThemePreferencesManager(context, "test_theme_prefs")
        assertEquals(ThemeMode.DARK, newManager.getThemeMode())
        assertEquals(ThemeMode.DARK, newManager.themeModeState.value)

        // Switch to LIGHT
        newManager.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, newManager.getThemeMode())

        val thirdManager = SharedPreferencesThemePreferencesManager(context, "test_theme_prefs")
        assertEquals(ThemeMode.LIGHT, thirdManager.getThemeMode())
    }

    @Test
    fun `corrupted or unknown theme string gracefully falls back to SYSTEM`() {
        context.getSharedPreferences("test_theme_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("key_theme_mode", "INVALID_UNKNOWN_MODE")
            .commit()

        val corruptedManager = SharedPreferencesThemePreferencesManager(context, "test_theme_prefs")
        assertEquals(ThemeMode.SYSTEM, corruptedManager.getThemeMode())
    }
}
