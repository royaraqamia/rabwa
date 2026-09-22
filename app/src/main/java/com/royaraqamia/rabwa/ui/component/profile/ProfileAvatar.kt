package com.royaraqamia.rabwa.ui.component.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.royaraqamia.rabwa.R
import com.royaraqamia.rabwa.core.avatar.AvatarPresetStyle
import com.royaraqamia.rabwa.core.avatar.GeneratedAvatarUtils
import com.royaraqamia.rabwa.ui.component.AppCircularProgressIndicator
import com.royaraqamia.rabwa.ui.icon.Camera
import com.royaraqamia.rabwa.ui.icon.ImageIcon
import com.royaraqamia.rabwa.ui.icon.LucideIcons
import com.royaraqamia.rabwa.ui.icon.Palette
import com.royaraqamia.rabwa.ui.icon.Sparkles
import com.royaraqamia.rabwa.ui.icon.User
import com.royaraqamia.rabwa.ui.theme.spacing

@Composable
fun ProfileAvatar(
    avatarUrl: String?,
    userDisplayName: String?,
    userEmail: String?,
    isUploading: Boolean = false,
    onAvatarCaptured: (ByteArray) -> Unit,
    onAvatarUrlSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPickerOpen by remember { mutableStateOf(false) }
    var isImageError by remember(avatarUrl) { mutableStateOf(false) }

    val initialLetter = remember(userDisplayName, userEmail) {
        val name = userDisplayName?.takeIf { it.isNotBlank() }
            ?: userEmail?.takeIf { it.isNotBlank() }
            ?: "A"
        name.first().uppercase()
    }

    Box(
        modifier = modifier.testTag("profile_avatar_container"),
        contentAlignment = Alignment.Center
    ) {
        // Main Avatar Circle
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(enabled = !isUploading) { isPickerOpen = true }
                .testTag("change_avatar_button"),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullBlinkingOrBlank() && !isImageError) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.nav_profile),
                    contentScale = ContentScale.Crop,
                    onError = { isImageError = true },
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .testTag("profile_avatar_image")
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(92.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initialLetter,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("profile_avatar_initials")
                        )
                    }
                }
            }

            // Uploading progress overlay
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppCircularProgressIndicator(
                        size = 32.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        testTag = "avatar_uploading_indicator"
                    )
                }
            }
        }

        // Camera Action Badge
        if (!isUploading) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .clickable { isPickerOpen = true }
                    .testTag("avatar_camera_badge")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = LucideIcons.Camera,
                        contentDescription = "تغيير الصورة الشخصية",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Avatar Selection Dialog Modal
    if (isPickerOpen) {
        AvatarPickerDialog(
            initials = initialLetter,
            onDismiss = { isPickerOpen = false },
            onImageCaptured = { bytes ->
                isPickerOpen = false
                onAvatarCaptured(bytes)
            },
            onPresetGenerated = { bytes ->
                isPickerOpen = false
                onAvatarCaptured(bytes)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarPickerDialog(
    initials: String,
    onDismiss: () -> Unit,
    onImageCaptured: (ByteArray) -> Unit,
    onPresetGenerated: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    var selectedPreset by remember { mutableStateOf(AvatarPresetStyle.WARM_SUNSET) }
    var customInitials by remember { mutableStateOf(initials) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val jpegBytes = GeneratedAvatarUtils.compressBitmapToJpegBytes(bitmap)
            onImageCaptured(jpegBytes)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bytes = GeneratedAvatarUtils.compressUriToJpegBytes(context, uri)
            if (bytes != null) {
                onImageCaptured(bytes)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("avatar_picker_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(bottom = MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Camera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "اختيار صورة الملف الشخصي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Camera & Gallery Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("take_photo_option"),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = LucideIcons.Camera,
                        contentDescription = "التقاط صورة بالكاميرا",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("الكاميرا", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pick_gallery_option"),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = LucideIcons.ImageIcon,
                        contentDescription = "اختيار من المعرض",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المعرض", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            // Section Header: Generated Avatar Placeholders
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.Sparkles,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "صور رمزية مبتكرة جاهزة",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Initials Text Input
            OutlinedTextField(
                value = customInitials,
                onValueChange = { if (it.length <= 2) customInitials = it },
                label = { Text("الأحرف الأولى") },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth()
            )

            // Grid of Preset Styles
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("pick_placeholder_option"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AvatarPresetStyle.entries.toTypedArray()) { preset ->
                    val isSelected = preset == selectedPreset
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable { selectedPreset = preset },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.startColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customInitials.take(2).uppercase().ifEmpty { "A" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(preset.textColorHex),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = preset.titleAr,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Apply Generated Avatar Button
            Button(
                onClick = {
                    val bitmap = GeneratedAvatarUtils.generateAvatarBitmap(
                        preset = selectedPreset,
                        initials = customInitials
                    )
                    val bytes = GeneratedAvatarUtils.bitmapToJpegBytes(bitmap)
                    onPresetGenerated(bytes)
                },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_avatar_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = LucideIcons.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ وتطبيق الصورة المبتكرة", fontWeight = FontWeight.Bold)
            }

            // Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    shape = CircleShape
                ) {
                    Text("إلغاء")
                }
            }
        }
    }
}

private fun String?.isNullBlinkingOrBlank(): Boolean {
    return this == null || this.isBlank() || this == "null"
}
