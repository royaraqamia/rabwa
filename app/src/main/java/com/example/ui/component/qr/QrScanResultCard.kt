package com.example.ui.component.qr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.qr.QrContentType
import com.example.domain.model.qr.QrScanResult
import com.example.ui.icon.CheckCircle2
import com.example.ui.icon.Copy
import com.example.ui.icon.ExternalLink
import com.example.ui.icon.LucideIcons
import com.example.ui.theme.spacing

@Composable
fun QrScanResultCard(
    result: QrScanResult,
    copiedFeedback: Boolean,
    onCopy: () -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_result_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            // Header Row: Type Badge + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (copiedFeedback) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.CheckCircle2,
                            contentDescription = "تم النسخ",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "تم النسخ الحافظة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Scanned Content Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(MaterialTheme.spacing.small)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = result.sanitizedContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("qr_scanned_text_content")
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy button
                FilledTonalButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("QR Code", result.sanitizedContent)
                        clipboard?.setPrimaryClip(clip)
                        onCopy()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("qr_copy_button")
                ) {
                    Icon(
                        imageVector = LucideIcons.Copy,
                        contentDescription = "نسخ النص",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ")
                }

                // Open Action button (URL, Phone, Email, Location)
                if (result.isUrl) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.actionUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_open_url_button")
                    ) {
                        Icon(
                            imageVector = LucideIcons.ExternalLink,
                            contentDescription = "فتح الرابط",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح الرابط", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else if (result.isPhone) {
                    Button(
                        onClick = {
                            try {
                                val telUri = if (result.sanitizedContent.startsWith("tel:")) result.sanitizedContent else "tel:${result.sanitizedContent}"
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(telUri))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_call_phone_button")
                    ) {
                        Text("اتصال", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else if (result.isEmail) {
                    Button(
                        onClick = {
                            try {
                                val mailUri = if (result.sanitizedContent.startsWith("mailto:")) result.sanitizedContent else "mailto:${result.sanitizedContent}"
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailUri))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_send_email_button")
                    ) {
                        Text("إرسال بريد", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else if (result.isGeo) {
                    Button(
                        onClick = {
                            try {
                                val geoUri = if (result.sanitizedContent.startsWith("geo:")) result.sanitizedContent else "geo:${result.sanitizedContent}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_open_map_button")
                    ) {
                        Text("فتح الخريطة", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Rescan button
                OutlinedButton(
                    onClick = onRescan,
                    modifier = Modifier.testTag("qr_rescan_button")
                ) {
                    Text("مسح مجدداً")
                }
            }
        }
    }
}
