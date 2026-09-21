package com.example.data.local.theme

import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface ThemePreferencesManager {
    val themeModeState: StateFlow<ThemeMode>
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
}
