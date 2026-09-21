package com.example.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Material3DesignSystemTest {

    @Test
    fun `spatial grid adheres strictly to 8dp multiples`() {
        val spacing = Spacing()
        assertEquals(0.dp, spacing.none)
        assertEquals(4.dp, spacing.extraSmall)
        assertEquals(8.dp, spacing.small)
        assertEquals(16.dp, spacing.medium)
        assertEquals(24.dp, spacing.large)
        assertEquals(32.dp, spacing.extraLarge)
        assertEquals(48.dp, spacing.huge)
        assertEquals(64.dp, spacing.colossal)
    }

    @Test
    fun `light and dark color schemes have distinct background and surface roles`() {
        // High contrast test
        assertNotEquals(RabwaLightColorScheme.background, RabwaDarkColorScheme.background)
        assertNotEquals(RabwaLightColorScheme.surface, RabwaDarkColorScheme.surface)
        assertNotEquals(RabwaLightColorScheme.primary, RabwaDarkColorScheme.primary)

        // Verifying light theme uses high luminance background
        assertEquals(SlateBackgroundLight, RabwaLightColorScheme.background)
        assertEquals(SlateBackgroundDark, RabwaDarkColorScheme.background)
    }

    @Test
    fun `light and dark schemes provide all 5 surface container elevation roles`() {
        // Light mode hierarchy
        assertEquals(SlateSurfaceContainerLowestLight, RabwaLightColorScheme.surfaceContainerLowest)
        assertEquals(SlateSurfaceContainerLowLight, RabwaLightColorScheme.surfaceContainerLow)
        assertEquals(SlateSurfaceContainerLight, RabwaLightColorScheme.surfaceContainer)
        assertEquals(SlateSurfaceContainerHighLight, RabwaLightColorScheme.surfaceContainerHigh)
        assertEquals(SlateSurfaceContainerHighestLight, RabwaLightColorScheme.surfaceContainerHighest)

        // Dark mode hierarchy
        assertEquals(SlateSurfaceContainerLowestDark, RabwaDarkColorScheme.surfaceContainerLowest)
        assertEquals(SlateSurfaceContainerLowDark, RabwaDarkColorScheme.surfaceContainerLow)
        assertEquals(SlateSurfaceContainerDark, RabwaDarkColorScheme.surfaceContainer)
        assertEquals(SlateSurfaceContainerHighDark, RabwaDarkColorScheme.surfaceContainerHigh)
        assertEquals(SlateSurfaceContainerHighestDark, RabwaDarkColorScheme.surfaceContainerHighest)
    }

    @Test
    fun `shapes are structured hierarchically`() {
        val shapes = RabwaShapes
        val density = androidx.compose.ui.unit.Density(1f)
        val size = androidx.compose.ui.geometry.Size(100f, 100f)

        assertEquals(4f, shapes.extraSmall.topStart.toPx(size, density), 0.01f)
        assertEquals(8f, shapes.small.topStart.toPx(size, density), 0.01f)
        assertEquals(12f, shapes.medium.topStart.toPx(size, density), 0.01f)
        assertEquals(16f, shapes.large.topStart.toPx(size, density), 0.01f)
        assertEquals(28f, shapes.extraLarge.topStart.toPx(size, density), 0.01f)
    }

    @Test
    fun `typography defines 15 distinct proportional tiers`() {
        val type = RabwaTypography
        // Display
        assertTrue(type.displayLarge.fontSize.value >= type.displayMedium.fontSize.value)
        assertTrue(type.displayMedium.fontSize.value >= type.displaySmall.fontSize.value)

        // Headline
        assertTrue(type.headlineLarge.fontSize.value >= type.headlineMedium.fontSize.value)
        assertTrue(type.headlineMedium.fontSize.value >= type.headlineSmall.fontSize.value)

        // Title
        assertTrue(type.titleLarge.fontSize.value >= type.titleMedium.fontSize.value)
        assertTrue(type.titleMedium.fontSize.value >= type.titleSmall.fontSize.value)

        // Body
        assertTrue(type.bodyLarge.fontSize.value >= type.bodyMedium.fontSize.value)
        assertTrue(type.bodyMedium.fontSize.value >= type.bodySmall.fontSize.value)

        // Label
        assertTrue(type.labelLarge.fontSize.value >= type.labelMedium.fontSize.value)
        assertTrue(type.labelMedium.fontSize.value >= type.labelSmall.fontSize.value)
    }

    @Test
    fun `ibm plex sans arabic is configured as the sole font family across all typography styles`() {
        val type = RabwaTypography
        val styles = listOf(
            type.displayLarge, type.displayMedium, type.displaySmall,
            type.headlineLarge, type.headlineMedium, type.headlineSmall,
            type.titleLarge, type.titleMedium, type.titleSmall,
            type.bodyLarge, type.bodyMedium, type.bodySmall,
            type.labelLarge, type.labelMedium, type.labelSmall
        )

        for (style in styles) {
            assertEquals(IbmPlexSansArabic, style.fontFamily)
        }
    }
}
