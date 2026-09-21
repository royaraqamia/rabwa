package com.example.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Shadcn UI (Lucide) Icon system for Jetpack Compose.
 * All icons follow Lucide's 24x24 viewport with standard 2dp stroke width,
 * round cap, and round join for clean, minimalist aesthetics.
 */
object LucideIcons {
    internal fun lucideIcon(
        name: String,
        block: PathBuilder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
            ) {
                block()
            }
        }.build()
    }
}
