package com.royaraqamia.rabwa.data.local.theme

import android.content.Context
import android.content.SharedPreferences
import com.royaraqamia.rabwa.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesThemePreferencesManager(
    private val context: Context,
    private val prefsName: String = PREFS_NAME
) : ThemePreferencesManager {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    private val _themeModeState = MutableStateFlow(getThemeMode())
    override val themeModeState: StateFlow<ThemeMode> = _themeModeState.asStateFlow()

    override fun getThemeMode(): ThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return try {
            ThemeMode.valueOf(savedName)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeState.value = mode
    }

    companion object {
        private const val PREFS_NAME = "app_theme_preferences"
        private const val KEY_THEME_MODE = "key_theme_mode"

        @Volatile
        private var instance: SharedPreferencesThemePreferencesManager? = null

        fun getInstance(context: Context): SharedPreferencesThemePreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesThemePreferencesManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
