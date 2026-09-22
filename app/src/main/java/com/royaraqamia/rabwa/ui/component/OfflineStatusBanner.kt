package com.royaraqamia.rabwa.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.royaraqamia.rabwa.R
import com.royaraqamia.rabwa.domain.model.SyncSummary
import com.royaraqamia.rabwa.ui.icon.CloudCheck
import com.royaraqamia.rabwa.ui.icon.CloudOff
import com.royaraqamia.rabwa.ui.icon.LucideIcons
import com.royaraqamia.rabwa.ui.icon.RefreshCw
import com.royaraqamia.rabwa.ui.theme.spacing

/**
 * Non-intrusive, accessible banner that displays offline-mode status and sync controls.
 * Strictly adheres to enterprise UX by never blocking user interaction.
 */
@Composable
fun OfflineStatusBanner(
    isOffline: Boolean,
    syncSummary: SyncSummary,
    isSyncing: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline || syncSummary.pendingCount > 0 || isSyncing,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offline_status_banner"),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (isOffline) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when {
                            isSyncing -> LucideIcons.RefreshCw
                            isOffline -> LucideIcons.CloudOff
                            else -> LucideIcons.CloudCheck
                        },
                        contentDescription = null,
                        tint = if (isOffline) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = when {
                            isSyncing -> stringResource(R.string.sync_status_syncing)
                            isOffline -> stringResource(R.string.offline_mode_banner)
                            syncSummary.pendingCount > 0 -> stringResource(R.string.sync_status_pending, syncSummary.pendingCount)
                            else -> stringResource(R.string.sync_status_all_synced)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isOffline) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }

                if (!isOffline && syncSummary.pendingCount > 0 && !isSyncing) {
                    TextButton(
                        onClick = onSyncNow,
                        modifier = Modifier.testTag("sync_now_button")
                    ) {
                        Text(
                            text = stringResource(R.string.sync_action_sync_now),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
