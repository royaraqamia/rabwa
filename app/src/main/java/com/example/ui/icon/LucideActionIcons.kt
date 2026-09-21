package com.example.ui.icon

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lucide action and input icons for Shadcn UI styling.
 */
val LucideIcons.Plus: ImageVector
    get() = lucideIcon("LucidePlus") {
        // line: 12 5 to 12 19
        moveTo(12f, 5f)
        verticalLineTo(19f)
        // line: 5 12 to 19 12
        moveTo(5f, 12f)
        horizontalLineTo(19f)
    }

val LucideIcons.Trash2: ImageVector
    get() = lucideIcon("LucideTrash2") {
        // path: M3 6h18
        moveTo(3f, 6f)
        horizontalLineTo(21f)
        // path: M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6
        moveTo(19f, 6f)
        verticalLineTo(20f)
        curveTo(19f, 20.53f, 18.79f, 21.04f, 18.41f, 21.41f)
        curveTo(18.04f, 21.79f, 17.53f, 22f, 17f, 22f)
        horizontalLineTo(7f)
        curveTo(6.47f, 22f, 5.96f, 21.79f, 5.59f, 21.41f)
        curveTo(5.21f, 21.04f, 5f, 20.53f, 5f, 20f)
        verticalLineTo(6f)
        // path: M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2
        moveTo(8f, 6f)
        verticalLineTo(4f)
        curveTo(8f, 3.47f, 8.21f, 2.96f, 8.59f, 2.59f)
        curveTo(8.96f, 2.21f, 9.47f, 2f, 10f, 2f)
        horizontalLineTo(14f)
        curveTo(14.53f, 2f, 15.04f, 2.21f, 15.41f, 2.59f)
        curveTo(15.79f, 2.96f, 16f, 3.47f, 16f, 4f)
        verticalLineTo(6f)
        // line: 10 11 to 10 17
        moveTo(10f, 11f)
        verticalLineTo(17f)
        // line: 14 11 to 14 17
        moveTo(14f, 11f)
        verticalLineTo(17f)
    }

val LucideIcons.PenLine: ImageVector
    get() = lucideIcon("LucidePenLine") {
        // path: M12 20h9
        moveTo(12f, 20f)
        horizontalLineTo(21f)
        // path: M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z
        moveTo(16.5f, 3.5f)
        curveTo(17.33f, 2.67f, 18.67f, 2.67f, 19.5f, 3.5f)
        curveTo(20.33f, 4.33f, 20.33f, 5.67f, 19.5f, 6.5f)
        lineTo(7f, 19f)
        lineTo(3f, 20f)
        lineTo(4f, 16f)
        close()
    }

val LucideIcons.Eye: ImageVector
    get() = lucideIcon("LucideEye") {
        // path: M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z
        moveTo(2f, 12f)
        curveTo(5f, 5.5f, 9f, 5f, 12f, 5f)
        curveTo(15f, 5f, 19f, 5.5f, 22f, 12f)
        curveTo(19f, 18.5f, 15f, 19f, 12f, 19f)
        curveTo(9f, 19f, 5f, 18.5f, 2f, 12f)
        close()
        // circle: cx=12 cy=12 r=3
        moveTo(15f, 12f)
        curveTo(15f, 13.66f, 13.66f, 15f, 12f, 15f)
        curveTo(10.34f, 15f, 9f, 13.66f, 9f, 12f)
        curveTo(9f, 10.34f, 10.34f, 9f, 12f, 9f)
        curveTo(13.66f, 9f, 15f, 10.34f, 15f, 12f)
        close()
    }

val LucideIcons.EyeOff: ImageVector
    get() = lucideIcon("LucideEyeOff") {
        // path: M9.88 9.88a3 3 0 1 0 4.24 4.24
        moveTo(10f, 10f)
        curveTo(9.37f, 10.63f, 9f, 11.27f, 9f, 12f)
        curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
        curveTo(12.73f, 15f, 13.37f, 14.63f, 14f, 14f)
        // path: 10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68
        moveTo(10.73f, 5.08f)
        curveTo(11.15f, 5.03f, 11.57f, 5f, 12f, 5f)
        curveTo(15f, 5f, 19f, 5.5f, 22f, 12f)
        curveTo(21.2f, 13.72f, 20.08f, 15.22f, 18.7f, 16.38f)
        // path: 6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61
        moveTo(6.61f, 6.61f)
        curveTo(4.71f, 8.08f, 3.22f, 9.94f, 2f, 12f)
        curveTo(5f, 18.5f, 9f, 19f, 12f, 19f)
        curveTo(14.07f, 19f, 16.03f, 18.39f, 17.65f, 17.39f)
        // line: 2 2 to 22 22
        moveTo(2f, 2f)
        lineTo(22f, 22f)
    }

val LucideIcons.QrCode: ImageVector
    get() = lucideIcon("LucideQrCode") {
        // Top-left finder pattern
        moveTo(4f, 3f)
        horizontalLineTo(8f)
        curveTo(8.55f, 3f, 9f, 3.45f, 9f, 4f)
        verticalLineTo(8f)
        curveTo(9f, 8.55f, 8.55f, 9f, 8f, 9f)
        horizontalLineTo(4f)
        curveTo(3.45f, 9f, 3f, 8.55f, 3f, 8f)
        verticalLineTo(4f)
        curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
        close()
        // Top-left center dot
        moveTo(6f, 6f)
        horizontalLineTo(6.01f)

        // Top-right finder pattern
        moveTo(16f, 3f)
        horizontalLineTo(20f)
        curveTo(20.55f, 3f, 21f, 3.45f, 21f, 4f)
        verticalLineTo(8f)
        curveTo(21f, 8.55f, 20.55f, 9f, 20f, 9f)
        horizontalLineTo(16f)
        curveTo(15.45f, 9f, 15f, 8.55f, 15f, 8f)
        verticalLineTo(4f)
        curveTo(15f, 3.45f, 15.45f, 3f, 16f, 3f)
        close()
        // Top-right center dot
        moveTo(18f, 6f)
        horizontalLineTo(18.01f)

        // Bottom-left finder pattern
        moveTo(4f, 15f)
        horizontalLineTo(8f)
        curveTo(8.55f, 15f, 9f, 15.45f, 9f, 16f)
        verticalLineTo(20f)
        curveTo(9f, 20.55f, 8.55f, 21f, 8f, 21f)
        horizontalLineTo(4f)
        curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
        verticalLineTo(16f)
        curveTo(3f, 15.45f, 3.45f, 15f, 4f, 15f)
        close()
        // Bottom-left center dot
        moveTo(6f, 18f)
        horizontalLineTo(6.01f)

        // QR timing and data modules
        moveTo(21f, 16f)
        verticalLineTo(20f)
        curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
        horizontalLineTo(16f)

        moveTo(12f, 3f)
        verticalLineTo(7f)
        curveTo(12f, 7.55f, 12.45f, 8f, 13f, 8f)
        horizontalLineTo(13.5f)

        moveTo(12f, 12f)
        horizontalLineTo(12.01f)

        moveTo(16f, 12f)
        horizontalLineTo(21f)

        moveTo(12f, 17f)
        verticalLineTo(21f)

        moveTo(17f, 17f)
        horizontalLineTo(17.01f)

        moveTo(7f, 12f)
        horizontalLineTo(9f)
    }

val LucideIcons.Flashlight: ImageVector
    get() = lucideIcon("LucideFlashlight") {
        // path: M18 6 6 18
        moveTo(18f, 6f)
        lineTo(6f, 18f)
        // path: m2 10 10-10
        moveTo(2f, 10f)
        lineTo(12f, 2f)
        // path: m12 22 10-10
        moveTo(12f, 22f)
        lineTo(22f, 12f)
    }

val LucideIcons.Copy: ImageVector
    get() = lucideIcon("LucideCopy") {
        // rect width=13 height=13 x=9 y=9 rx=2
        moveTo(11f, 9f)
        horizontalLineTo(20f)
        curveTo(21.1f, 9f, 22f, 9.9f, 22f, 11f)
        verticalLineTo(20f)
        curveTo(22f, 21.1f, 21.1f, 22f, 20f, 22f)
        horizontalLineTo(11f)
        curveTo(9.9f, 22f, 9f, 21.1f, 9f, 20f)
        verticalLineTo(11f)
        curveTo(9f, 9.9f, 9.9f, 9f, 11f, 9f)
        close()
        // path: M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1
        moveTo(5f, 15f)
        horizontalLineTo(4f)
        curveTo(2.9f, 15f, 2f, 14.1f, 2f, 13f)
        verticalLineTo(4f)
        curveTo(2f, 2.9f, 2.9f, 2f, 4f, 2f)
        horizontalLineTo(13f)
        curveTo(14.1f, 2f, 15f, 2.9f, 15f, 4f)
        verticalLineTo(5f)
    }

val LucideIcons.ExternalLink: ImageVector
    get() = lucideIcon("LucideExternalLink") {
        // path: M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6
        moveTo(18f, 13f)
        verticalLineTo(19f)
        curveTo(18f, 20.1f, 17.1f, 21f, 16f, 21f)
        horizontalLineTo(5f)
        curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
        verticalLineTo(8f)
        curveTo(3f, 6.9f, 3.9f, 6f, 5f, 6f)
        horizontalLineTo(11f)
        // path: 15 3h6v6
        moveTo(15f, 3f)
        horizontalLineTo(21f)
        verticalLineTo(9f)
        // line: 10 14 21 3
        moveTo(10f, 14f)
        lineTo(21f, 3f)
    }

val LucideIcons.Camera: ImageVector
    get() = lucideIcon("LucideCamera") {
        moveTo(14.5f, 4f)
        horizontalLineTo(9.5f)
        lineTo(7f, 7f)
        horizontalLineTo(4f)
        curveTo(2.9f, 7f, 2f, 7.9f, 2f, 9f)
        verticalLineTo(18f)
        curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
        horizontalLineTo(20f)
        curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
        verticalLineTo(9f)
        curveTo(22f, 7.9f, 21.1f, 7f, 20f, 7f)
        horizontalLineTo(17f)
        lineTo(14.5f, 4f)
        close()

        moveTo(15f, 13f)
        curveTo(15f, 14.66f, 13.66f, 16f, 12f, 16f)
        curveTo(10.34f, 16f, 9f, 14.66f, 9f, 13f)
        curveTo(9f, 11.34f, 10.34f, 10f, 12f, 10f)
        curveTo(13.66f, 10f, 15f, 11.34f, 15f, 13f)
        close()
    }

val LucideIcons.ImageIcon: ImageVector
    get() = lucideIcon("LucideImageIcon") {
        moveTo(5f, 3f)
        horizontalLineTo(19f)
        curveTo(20.1f, 3f, 21f, 3.9f, 21f, 5f)
        verticalLineTo(19f)
        curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
        horizontalLineTo(5f)
        curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
        verticalLineTo(5f)
        curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
        close()

        moveTo(10f, 8.5f)
        curveTo(10f, 9.33f, 9.33f, 10f, 8.5f, 10f)
        curveTo(7.67f, 10f, 7f, 9.33f, 7f, 8.5f)
        curveTo(7f, 7.67f, 7.67f, 7f, 8.5f, 7f)
        curveTo(9.33f, 7f, 10f, 7.67f, 10f, 8.5f)
        close()

        moveTo(21f, 15f)
        lineTo(17.91f, 11.91f)
        curveTo(17.16f, 11.16f, 15.84f, 11.16f, 15.09f, 11.91f)
        lineTo(6f, 21f)
    }

val LucideIcons.Sparkles: ImageVector
    get() = lucideIcon("LucideSparkles") {
        moveTo(12f, 3f)
        lineTo(13.5f, 8.5f)
        lineTo(19f, 10f)
        lineTo(13.5f, 11.5f)
        lineTo(12f, 17f)
        lineTo(10.5f, 11.5f)
        lineTo(5f, 10f)
        lineTo(10.5f, 8.5f)
        close()
    }

val LucideIcons.Palette: ImageVector
    get() = lucideIcon("LucidePalette") {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(13.38f, 22f, 14.5f, 20.88f, 14.5f, 19.5f)
        curveTo(14.5f, 18.86f, 14.25f, 18.28f, 13.85f, 17.85f)
        curveTo(13.46f, 17.42f, 13.25f, 16.86f, 13.25f, 16.25f)
        curveTo(13.25f, 15f, 14.25f, 14f, 15.5f, 14f)
        horizontalLineTo(17f)
        curveTo(19.76f, 14f, 22f, 11.76f, 22f, 9f)
        curveTo(22f, 5.13f, 17.52f, 2f, 12f, 2f)
        close()
    }

