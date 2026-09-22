package com.royaraqamia.rabwa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enterprise Spatial Design Tokens based on the standard 8.dp grid system.
 * Enforces visual balance, consistent negative space, and touch ergonomics.
 */
data class Spacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val colossal: Dp = 64.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Convenient accessor to design system spacing tokens via MaterialTheme.spacing.
 */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
