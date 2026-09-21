package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// =============================================================================
// Core Palette Tokens: "رَبْوَة" (Rabwa - Elevated Terrain & Modern Vitality)
// =============================================================================

// Emerald & Teal Tones (Elevated Nature)
val EmeraldPrimaryLight = Color(0xFF047857)
val EmeraldOnPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFD1FAE5)
val EmeraldOnPrimaryContainerLight = Color(0xFF064E3B)

val EmeraldPrimaryDark = Color(0xFF34D399)
val EmeraldOnPrimaryDark = Color(0xFF064E3B)
val EmeraldPrimaryContainerDark = Color(0xFF065F46)
val EmeraldOnPrimaryContainerDark = Color(0xFFA7F3D0)

// Sky & Cyan Tones (Secondary - Precision & Horizon)
val SkySecondaryLight = Color(0xFF0284C7)
val SkyOnSecondaryLight = Color(0xFFFFFFFF)
val SkySecondaryContainerLight = Color(0xFFE0F2FE)
val SkyOnSecondaryContainerLight = Color(0xFF0C4A6E)

val SkySecondaryDark = Color(0xFF38BDF8)
val SkyOnSecondaryDark = Color(0xFF0C4A6E)
val SkySecondaryContainerDark = Color(0xFF0369A1)
val SkyOnSecondaryContainerDark = Color(0xFFBAE6FD)

// Amber & Ochre Tones (Tertiary - Sun & Elevated Warmth)
val OchreTertiaryLight = Color(0xFFD97706)
val OchreOnTertiaryLight = Color(0xFFFFFFFF)
val OchreTertiaryContainerLight = Color(0xFFFEF3C7)
val OchreOnTertiaryContainerLight = Color(0xFF78350F)

val OchreTertiaryDark = Color(0xFFFBBF24)
val OchreOnTertiaryDark = Color(0xFF78350F)
val OchreTertiaryContainerDark = Color(0xFF92400E)
val OchreOnTertiaryContainerDark = Color(0xFFFDE68A)

// Neutral & Slate Tones (Surfaces & Backgrounds)
val SlateBackgroundLight = Color(0xFFF8FAFC)
val SlateOnBackgroundLight = Color(0xFF0F172A)
val SlateSurfaceLight = Color(0xFFFFFFFF)
val SlateOnSurfaceLight = Color(0xFF0F172A)
val SlateSurfaceVariantLight = Color(0xFFF1F5F9)
val SlateOnSurfaceVariantLight = Color(0xFF475569)
val SlateOutlineLight = Color(0xFFCBD5E1)
val SlateOutlineVariantLight = Color(0xFFE2E8F0)

val SlateSurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SlateSurfaceContainerLowLight = Color(0xFFF8FAFC)
val SlateSurfaceContainerLight = Color(0xFFF1F5F9)
val SlateSurfaceContainerHighLight = Color(0xFFE2E8F0)
val SlateSurfaceContainerHighestLight = Color(0xFFCBD5E1)

// Dark Neutrals
val SlateBackgroundDark = Color(0xFF0A0F1D)
val SlateOnBackgroundDark = Color(0xFFF8FAFC)
val SlateSurfaceDark = Color(0xFF0F172A)
val SlateOnSurfaceDark = Color(0xFFF8FAFC)
val SlateSurfaceVariantDark = Color(0xFF1E293B)
val SlateOnSurfaceVariantDark = Color(0xFF94A3B8)
val SlateOutlineDark = Color(0xFF475569)
val SlateOutlineVariantDark = Color(0xFF334155)

val SlateSurfaceContainerLowestDark = Color(0xFF070B14)
val SlateSurfaceContainerLowDark = Color(0xFF0F172A)
val SlateSurfaceContainerDark = Color(0xFF1E293B)
val SlateSurfaceContainerHighDark = Color(0xFF283548)
val SlateSurfaceContainerHighestDark = Color(0xFF334155)

// Status & Error Tones
val ErrorRed = Color(0xFFEF4444)
val OnErrorRed = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF991B1B)

val ErrorRedDark = Color(0xFFF87171)
val ErrorContainerDark = Color(0xFF7F1D1D)
val OnErrorContainerDark = Color(0xFFFECACA)

val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)

// =============================================================================
// Complete Material 3 Color Schemes
// =============================================================================

val RabwaLightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = EmeraldOnPrimaryContainerLight,
    inversePrimary = EmeraldPrimaryDark,

    secondary = SkySecondaryLight,
    onSecondary = SkyOnSecondaryLight,
    secondaryContainer = SkySecondaryContainerLight,
    onSecondaryContainer = SkyOnSecondaryContainerLight,

    tertiary = OchreTertiaryLight,
    onTertiary = OchreOnTertiaryLight,
    tertiaryContainer = OchreTertiaryContainerLight,
    onTertiaryContainer = OchreOnTertiaryContainerLight,

    background = SlateBackgroundLight,
    onBackground = SlateOnBackgroundLight,

    surface = SlateSurfaceLight,
    onSurface = SlateOnSurfaceLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateOnSurfaceVariantLight,
    surfaceTint = EmeraldPrimaryLight,

    surfaceContainerLowest = SlateSurfaceContainerLowestLight,
    surfaceContainerLow = SlateSurfaceContainerLowLight,
    surfaceContainer = SlateSurfaceContainerLight,
    surfaceContainerHigh = SlateSurfaceContainerHighLight,
    surfaceContainerHighest = SlateSurfaceContainerHighestLight,

    inverseSurface = SlateBackgroundDark,
    inverseOnSurface = SlateOnBackgroundDark,

    outline = SlateOutlineLight,
    outlineVariant = SlateOutlineVariantLight,

    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    scrim = Color(0xFF000000)
)

val RabwaDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldPrimaryContainerDark,
    onPrimaryContainer = EmeraldOnPrimaryContainerDark,
    inversePrimary = EmeraldPrimaryLight,

    secondary = SkySecondaryDark,
    onSecondary = SkyOnSecondaryDark,
    secondaryContainer = SkySecondaryContainerDark,
    onSecondaryContainer = SkyOnSecondaryContainerDark,

    tertiary = OchreTertiaryDark,
    onTertiary = OchreOnTertiaryDark,
    tertiaryContainer = OchreTertiaryContainerDark,
    onTertiaryContainer = OchreOnTertiaryContainerDark,

    background = SlateBackgroundDark,
    onBackground = SlateOnBackgroundDark,

    surface = SlateSurfaceDark,
    onSurface = SlateOnSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateOnSurfaceVariantDark,
    surfaceTint = EmeraldPrimaryDark,

    surfaceContainerLowest = SlateSurfaceContainerLowestDark,
    surfaceContainerLow = SlateSurfaceContainerLowDark,
    surfaceContainer = SlateSurfaceContainerDark,
    surfaceContainerHigh = SlateSurfaceContainerHighDark,
    surfaceContainerHighest = SlateSurfaceContainerHighestDark,

    inverseSurface = SlateBackgroundLight,
    inverseOnSurface = SlateOnBackgroundLight,

    outline = SlateOutlineDark,
    outlineVariant = SlateOutlineVariantDark,

    error = ErrorRedDark,
    onError = OnErrorRed,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    scrim = Color(0xFF000000)
)
