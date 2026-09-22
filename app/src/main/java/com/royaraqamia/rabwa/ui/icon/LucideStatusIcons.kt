package com.royaraqamia.rabwa.ui.icon

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lucide status & alert icons for Shadcn UI styling.
 */
val LucideIcons.CheckCircle2: ImageVector
    get() = lucideIcon("LucideCheckCircle2") {
        // circle cx=12 cy=12 r=10
        moveTo(22f, 12f)
        curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
        curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
        curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
        curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
        close()
        // polyline: m9 12 2 2 4-4
        moveTo(9f, 12f)
        lineTo(11f, 14f)
        lineTo(15f, 10f)
    }

val LucideIcons.AlertTriangle: ImageVector
    get() = lucideIcon("LucideAlertTriangle") {
        // path: m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z
        moveTo(21.73f, 18f)
        lineTo(13.73f, 4f)
        curveTo(13.37f, 3.37f, 12.72f, 3f, 12f, 3f)
        curveTo(11.28f, 3f, 10.63f, 3.37f, 10.27f, 4f)
        lineTo(2.27f, 18f)
        curveTo(1.91f, 18.63f, 1.93f, 19.41f, 2.31f, 20.03f)
        curveTo(2.69f, 20.65f, 3.36f, 21f, 4.08f, 21f)
        horizontalLineTo(19.92f)
        curveTo(20.64f, 21f, 21.31f, 20.65f, 21.69f, 20.03f)
        curveTo(22.07f, 19.41f, 22.09f, 18.63f, 21.73f, 18f)
        close()
        // line: 12 9 to 12 13
        moveTo(12f, 9f)
        verticalLineTo(13f)
        // line: 12 17 to 12.01 17
        moveTo(12f, 17f)
        horizontalLineTo(12.01f)
    }

val LucideIcons.AlertCircle: ImageVector
    get() = lucideIcon("LucideAlertCircle") {
        // circle cx=12 cy=12 r=10
        moveTo(22f, 12f)
        curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
        curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
        curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
        curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
        close()
        // line 12 8 to 12 12
        moveTo(12f, 8f)
        verticalLineTo(12f)
        // line 12 16 to 12.01 16
        moveTo(12f, 16f)
        horizontalLineTo(12.01f)
    }

val LucideIcons.RefreshCw: ImageVector
    get() = lucideIcon("LucideRefreshCw") {
        // path: M21 2v6h-6
        moveTo(21f, 2f)
        verticalLineTo(8f)
        horizontalLineTo(15f)
        // path: M3 12a9 9 0 0 1 15-6.7L21 8
        moveTo(3f, 12f)
        curveTo(3f, 9.51f, 4.03f, 7.27f, 5.71f, 5.67f)
        curveTo(7.39f, 4.07f, 9.61f, 3.1f, 12f, 3.1f)
        curveTo(15.2f, 3.1f, 18.06f, 4.77f, 19.68f, 7.3f)
        lineTo(21f, 8f)
        // path: M3 22v-6h6
        moveTo(3f, 22f)
        verticalLineTo(16f)
        horizontalLineTo(9f)
        // path: M21 12a9 9 0 0 1-15 6.7L3 16
        moveTo(21f, 12f)
        curveTo(21f, 14.49f, 19.97f, 16.73f, 18.29f, 18.33f)
        curveTo(16.61f, 19.93f, 14.39f, 20.9f, 12f, 20.9f)
        curveTo(8.8f, 20.9f, 5.94f, 19.23f, 4.32f, 16.7f)
        lineTo(3f, 16f)
    }

val LucideIcons.X: ImageVector
    get() = lucideIcon("LucideX") {
        // path: M18 6 6 18
        moveTo(18f, 6f)
        lineTo(6f, 18f)
        // path: M6 6 18 18
        moveTo(6f, 6f)
        lineTo(18f, 18f)
    }

val LucideIcons.Bell: ImageVector
    get() = lucideIcon("LucideBell") {
        // path: M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9
        moveTo(6f, 8f)
        curveTo(6f, 4.69f, 8.69f, 2f, 12f, 2f)
        curveTo(15.31f, 2f, 18f, 4.69f, 18f, 8f)
        curveTo(18f, 15f, 21f, 17f, 21f, 17f)
        horizontalLineTo(3f)
        curveTo(3f, 17f, 6f, 15f, 6f, 8f)
        close()
        // path: M10.3 21a1.94 1.94 0 0 0 3.4 0
        moveTo(10.3f, 21f)
        curveTo(10.66f, 21.62f, 11.28f, 22f, 12f, 22f)
        curveTo(12.72f, 22f, 13.34f, 21.62f, 13.7f, 21f)
    }

val LucideIcons.BellOff: ImageVector
    get() = lucideIcon("LucideBellOff") {
        // Line through: M2 2 22 22
        moveTo(2f, 2f)
        lineTo(22f, 22f)
        // Partial bell
        moveTo(8.7f, 3f)
        curveTo(9.7f, 2.36f, 10.82f, 2f, 12f, 2f)
        curveTo(15.31f, 2f, 18f, 4.69f, 18f, 8f)
        curveTo(18f, 9.8f, 18.2f, 11.6f, 18.6f, 13f)
        moveTo(17f, 17f)
        horizontalLineTo(3f)
        curveTo(3f, 17f, 6f, 15f, 6f, 8f)
        curveTo(6f, 7.4f, 6.1f, 6.8f, 6.3f, 6.3f)
        // Clapper: M10.3 21a1.94 1.94 0 0 0 3.4 0
        moveTo(10.3f, 21f)
        curveTo(10.66f, 21.62f, 11.28f, 22f, 12f, 22f)
        curveTo(12.72f, 22f, 13.34f, 21.62f, 13.7f, 21f)
    }

