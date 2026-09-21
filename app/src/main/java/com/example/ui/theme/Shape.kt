package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Enterprise Material 3 Shape Tokens for "رَبْوَة" (Rabwa).
 * Enforces fully rounded geometry across all buttons, text fields, chips, and bottom sheets.
 */
val RabwaShapes = Shapes(
    // Tags, status badges, tiny chips
    extraSmall = RoundedCornerShape(4.dp),

    // Small buttons, text input fields, tooltips
    small = RoundedCornerShape(8.dp),

    // Standard cards, buttons, medium containers
    medium = RoundedCornerShape(12.dp),

    // Hero banners, bottom sheets, prominent elevated surfaces
    large = RoundedCornerShape(16.dp),

    // Floating action buttons, full-surface modals
    extraLarge = RoundedCornerShape(28.dp)
)
