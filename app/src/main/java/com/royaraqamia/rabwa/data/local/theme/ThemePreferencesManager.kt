package com.royaraqamia.rabwa.data.local.theme

import com.royaraqamia.rabwa.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface ThemePreferencesManager {
    val themeModeState: StateFlow<ThemeMode>
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
}
