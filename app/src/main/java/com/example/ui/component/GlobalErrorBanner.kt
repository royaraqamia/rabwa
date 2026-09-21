package com.example.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.result.AppError
import com.example.presentation.error.ErrorMessageResolver
import com.example.presentation.error.UserFacingError
import com.example.ui.icon.AlertCircle
import com.example.ui.icon.LucideIcons
import com.example.ui.icon.RefreshCw
import com.example.ui.icon.X
import com.example.ui.theme.spacing

/**
 * Enterprise-grade global error handling UI component.
 *
 * Guarantees:
 * 1. Zero exposure of raw exception messages or stack traces.
 * 2. Material 3 tokens with clear visual feedback (errorContainer / onErrorContainer).
 * 3. Standard accessibility compliance (48dp minimum touch targets, proper content descriptions).
 * 4. Deterministic test tags for QA automated testing.
 * 5. Optional retry capability for transient failures.
 */
@Composable
fun GlobalErrorBanner(
    error: AppError?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        if (error != null) {
            val userFacingError: UserFacingError = ErrorMessageResolver.resolve(error)
            val context = LocalContext.current
            val localizedText = try {
                context.getString(userFacingError.messageResId)
            } catch (e: Exception) {
                userFacingError.fallbackMessage
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_error_banner"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = LucideIcons.AlertCircle,
                        contentDescription = stringResource(R.string.cd_error_alert),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("global_error_icon")
                    )

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = localizedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.testTag("global_error_message")
                        )

                        if (userFacingError.isRetryable && onRetry != null) {
                            TextButton(
                                onClick = onRetry,
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(top = MaterialTheme.spacing.extraSmall)
                                    .testTag("global_error_retry_button"),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = LucideIcons.RefreshCw,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Text(
                                    text = stringResource(R.string.action_retry),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("global_error_dismiss_button")
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = stringResource(R.string.action_dismiss),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
