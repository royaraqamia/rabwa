package com.example.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.spacing

/**
 * Enterprise-grade reusable circular progress indicator component.
 *
 * Guarantees:
 * 1. Material Design 3 compliance using semantic theme tokens.
 * 2. Accessibility standard compliance (meaningful content descriptions).
 * 3. Deterministic test tags for automated QA testing.
 * 4. Dual operational modes:
 *    - Compact inline mode for buttons, cards, or list items with optional label.
 *    - Standalone circular progress indicator.
 */
@Composable
fun AppCircularProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    strokeWidth: Dp = 2.5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    label: String? = null,
    testTag: String = "app_circular_progress_indicator"
) {
    val defaultContentDesc = stringResource(R.string.cd_loading_progress)
    val accessibleModifier = modifier
        .semantics { contentDescription = label ?: defaultContentDesc }
        .testTag(testTag)

    if (label != null) {
        Row(
            modifier = accessibleModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(size)
                    .testTag("${testTag}_spinner"),
                color = color,
                strokeWidth = strokeWidth,
                trackColor = trackColor,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                modifier = Modifier.testTag("${testTag}_label")
            )
        }
    } else {
        CircularProgressIndicator(
            modifier = accessibleModifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Enterprise-grade loading overlay component for asynchronous operations.
 *
 * Designed to be triggered across the UI during asynchronous network or database operations.
 * Prevents unintentional background user interaction while displaying a clear, localized
 * progress indication card with an animated circular indicator.
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.loading_generic),
    testTag: String = "loading_overlay"
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Intercept touch events to prevent background interaction */ }
                )
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(MaterialTheme.spacing.large)
                    .testTag("${testTag}_card")
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.medium
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppCircularProgressIndicator(
                        size = 36.dp,
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        testTag = "${testTag}_spinner"
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("${testTag}_message")
                    )
                }
            }
        }
    }
}
