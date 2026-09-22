package com.royaraqamia.rabwa.ui.component.qr

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Clean Material 3 viewfinder overlay for camera QR scanning.
 * Draws semi-transparent darkened surround, rounded framing corners, and a scanning beam.
 */
@Composable
fun QrViewfinderOverlay(
    modifier: Modifier = Modifier,
    isScanningActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "QrLaserScan")
    val laserFraction by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserPosition"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val cornerColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("qr_viewfinder_overlay")
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val boxDimension = (canvasWidth.coerceAtMost(canvasHeight) * 0.72f).coerceAtMost(280.dp.toPx())
        val left = (canvasWidth - boxDimension) / 2f
        val top = (canvasHeight - boxDimension) / 2.2f

        // Draw darkened backdrop with transparent cutout
        drawPath(
            path = Path().apply {
                // Outer full screen
                addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                // Inner cutout
                addRoundRect(
                    RoundRect(
                        left = left,
                        top = top,
                        right = left + boxDimension,
                        bottom = top + boxDimension,
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                )
            },
            color = Color.Black.copy(alpha = 0.55f),
            blendMode = BlendMode.SrcOver
        )

        // Draw outer subtle frame border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(left, top),
            size = Size(boxDimension, boxDimension),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw 4 corner accents
        val cornerLength = 32.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val cornerRadius = 24.dp.toPx()

        drawCornerAccents(
            left = left,
            top = top,
            boxSize = boxDimension,
            cornerLength = cornerLength,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            color = cornerColor
        )

        // Draw animated laser beam inside the scanning box
        if (isScanningActive) {
            val laserY = top + (boxDimension * laserFraction)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.1f),
                        primaryColor.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.1f)
                    ),
                    startX = left + 16.dp.toPx(),
                    endX = left + boxDimension - 16.dp.toPx()
                ),
                start = Offset(left + 16.dp.toPx(), laserY),
                end = Offset(left + boxDimension - 16.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawCornerAccents(
    left: Float,
    top: Float,
    boxSize: Float,
    cornerLength: Float,
    strokeWidth: Float,
    cornerRadius: Float,
    color: Color
) {
    val right = left + boxSize
    val bottom = top + boxSize
    val cap = StrokeCap.Round

    // Top-Left
    drawLine(color, Offset(left + cornerRadius, top), Offset(left + cornerLength, top), strokeWidth, cap)
    drawLine(color, Offset(left, top + cornerRadius), Offset(left, top + cornerLength), strokeWidth, cap)

    // Top-Right
    drawLine(color, Offset(right - cornerLength, top), Offset(right - cornerRadius, top), strokeWidth, cap)
    drawLine(color, Offset(right, top + cornerRadius), Offset(right, top + cornerLength), strokeWidth, cap)

    // Bottom-Left
    drawLine(color, Offset(left + cornerRadius, bottom), Offset(left + cornerLength, bottom), strokeWidth, cap)
    drawLine(color, Offset(left, bottom - cornerLength), Offset(left, bottom - cornerRadius), strokeWidth, cap)

    // Bottom-Right
    drawLine(color, Offset(right - cornerLength, bottom), Offset(right - cornerRadius, bottom), strokeWidth, cap)
    drawLine(color, Offset(right, bottom - cornerLength), Offset(right, bottom - cornerRadius), strokeWidth, cap)
}
