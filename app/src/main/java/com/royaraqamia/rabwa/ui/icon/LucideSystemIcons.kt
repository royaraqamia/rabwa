package com.royaraqamia.rabwa.ui.icon

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lucide system, theme, and connectivity icons for Shadcn UI styling.
 */
val LucideIcons.Sun: ImageVector
    get() = lucideIcon("LucideSun") {
        // circle cx=12 cy=12 r=4
        moveTo(16f, 12f)
        curveTo(16f, 14.21f, 14.21f, 16f, 12f, 16f)
        curveTo(9.79f, 16f, 8f, 14.21f, 8f, 12f)
        curveTo(8f, 9.79f, 9.79f, 8f, 12f, 8f)
        curveTo(14.21f, 8f, 16f, 9.79f, 16f, 12f)
        close()
        // Rays
        moveTo(12f, 2f); verticalLineTo(4f)
        moveTo(12f, 20f); verticalLineTo(22f)
        moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
        moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
        moveTo(2f, 12f); horizontalLineTo(4f)
        moveTo(20f, 12f); horizontalLineTo(22f)
        moveTo(6.34f, 17.66f); lineTo(4.93f, 19.07f)
        moveTo(19.07f, 4.93f); lineTo(17.66f, 6.34f)
    }

val LucideIcons.Moon: ImageVector
    get() = lucideIcon("LucideMoon") {
        // path: M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z
        moveTo(12f, 3f)
        curveTo(10.68f, 4.54f, 10f, 6.55f, 10f, 8.64f)
        curveTo(10f, 13.26f, 13.74f, 17f, 18.36f, 17f)
        curveTo(19.34f, 17f, 20.3f, 16.83f, 21.19f, 16.51f)
        curveTo(19.9f, 19.82f, 16.68f, 22.14f, 12.87f, 22f)
        curveTo(7.9f, 21.82f, 3.82f, 17.74f, 3.64f, 12.77f)
        curveTo(3.48f, 8.42f, 6.46f, 4.75f, 10.59f, 3.39f)
        curveTo(11.05f, 3.24f, 11.53f, 3.1f, 12f, 3f)
        close()
    }

val LucideIcons.Cloud: ImageVector
    get() = lucideIcon("LucideCloud") {
        // path: M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z
        moveTo(17.5f, 19f)
        horizontalLineTo(9f)
        curveTo(5.13f, 19f, 2f, 15.87f, 2f, 12f)
        curveTo(2f, 8.35f, 4.79f, 5.35f, 8.35f, 5.04f)
        curveTo(10.05f, 2.58f, 13.12f, 1.4f, 16.03f, 2.08f)
        curveTo(18.94f, 2.76f, 21.17f, 5.17f, 21.62f, 8.12f)
        curveTo(23.08f, 9.21f, 24f, 10.95f, 24f, 12.87f)
        curveTo(24f, 16.25f, 21.13f, 19f, 17.5f, 19f)
        close()
    }

val LucideIcons.CloudCheck: ImageVector
    get() = lucideIcon("LucideCloudCheck") {
        // path: M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z
        moveTo(17.5f, 19f)
        horizontalLineTo(9f)
        curveTo(5.13f, 19f, 2f, 15.87f, 2f, 12f)
        curveTo(2f, 8.35f, 4.79f, 5.35f, 8.35f, 5.04f)
        curveTo(10.05f, 2.58f, 13.12f, 1.4f, 16.03f, 2.08f)
        curveTo(18.94f, 2.76f, 21.17f, 5.17f, 21.62f, 8.12f)
        curveTo(23.08f, 9.21f, 24f, 10.95f, 24f, 12.87f)
        curveTo(24f, 16.25f, 21.13f, 19f, 17.5f, 19f)
        close()
        // polyline: m9 13 2 2 4-4
        moveTo(9f, 13f)
        lineTo(11f, 15f)
        lineTo(15f, 11f)
    }

val LucideIcons.CloudOff: ImageVector
    get() = lucideIcon("LucideCloudOff") {
        // line: 2 2 to 22 22
        moveTo(2f, 2f)
        lineTo(22f, 22f)
        // path fragments
        moveTo(5.78f, 5.78f)
        curveTo(3.48f, 7.15f, 2f, 9.42f, 2f, 12f)
        curveTo(2f, 15.87f, 5.13f, 19f, 9f, 19f)
        horizontalLineTo(19f)
        // right cloud part
        moveTo(21.73f, 16.27f)
        curveTo(23.11f, 15.34f, 24f, 13.88f, 24f, 12.18f)
        curveTo(24f, 9.71f, 22.04f, 7.71f, 19.62f, 7.71f)
        curveTo(19.23f, 4.67f, 16.92f, 2.29f, 13.88f, 2.03f)
        curveTo(11.83f, 1.86f, 9.87f, 2.77f, 8.64f, 4.36f)
    }

val LucideIcons.WifiOff: ImageVector
    get() = lucideIcon("LucideWifiOff") {
        // line: 2 2 to 22 22
        moveTo(2f, 2f)
        lineTo(22f, 22f)
        // arcs
        moveTo(8.5f, 16.5f)
        curveTo(9.56f, 15.44f, 10.98f, 14.85f, 12.47f, 14.85f)
        curveTo(13.97f, 14.85f, 15.39f, 15.44f, 16.45f, 16.5f)
        // dot
        moveTo(12f, 20f)
        horizontalLineTo(12.01f)
    }

val LucideIcons.Mail: ImageVector
    get() = lucideIcon("LucideMail") {
        // rect width=20 height=16 x=2 y=4 rx=2
        moveTo(4f, 4f)
        horizontalLineTo(20f)
        curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
        verticalLineTo(18f)
        curveTo(22f, 19.1f, 21.1f, 20f, 20f, 20f)
        horizontalLineTo(4f)
        curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
        verticalLineTo(6f)
        curveTo(2f, 4.9f, 2.9f, 4f, 4f, 4f)
        close()
        // path: m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7
        moveTo(22f, 7f)
        lineTo(13.03f, 12.7f)
        curveTo(12.41f, 13.09f, 11.59f, 13.09f, 10.97f, 12.7f)
        lineTo(2f, 7f)
    }

val LucideIcons.Lock: ImageVector
    get() = lucideIcon("LucideLock") {
        // rect width=18 height=11 x=3 y=11 rx=2 ry=2
        moveTo(5f, 11f)
        horizontalLineTo(19f)
        curveTo(20.1f, 11f, 21f, 11.9f, 21f, 13f)
        verticalLineTo(20f)
        curveTo(21f, 21.1f, 20.1f, 22f, 19f, 22f)
        horizontalLineTo(5f)
        curveTo(3.9f, 22f, 3f, 21.1f, 3f, 20f)
        verticalLineTo(13f)
        curveTo(3f, 11.9f, 3.9f, 11f, 5f, 11f)
        close()
        // path: M7 11V7a5 5 0 0 1 10 0v4
        moveTo(7f, 11f)
        verticalLineTo(7f)
        curveTo(7f, 4.24f, 9.24f, 2f, 12f, 2f)
        curveTo(14.76f, 2f, 17f, 4.24f, 17f, 7f)
        verticalLineTo(11f)
    }

val LucideIcons.Keyboard: ImageVector
    get() = lucideIcon("LucideKeyboard") {
        // rect width=20 height=16 x=2 y=4 rx=2 ry=2
        moveTo(4f, 4f)
        horizontalLineTo(20f)
        curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
        verticalLineTo(18f)
        curveTo(22f, 19.1f, 21.1f, 20f, 20f, 20f)
        horizontalLineTo(4f)
        curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
        verticalLineTo(6f)
        curveTo(2f, 4.9f, 2.9f, 4f, 4f, 4f)
        close()
        // Keys
        moveTo(6f, 8f); horizontalLineTo(6.01f)
        moveTo(10f, 8f); horizontalLineTo(10.01f)
        moveTo(14f, 8f); horizontalLineTo(14.01f)
        moveTo(18f, 8f); horizontalLineTo(18.01f)
        moveTo(6f, 12f); horizontalLineTo(6.01f)
        moveTo(18f, 12f); horizontalLineTo(18.01f)
        moveTo(7f, 16f); horizontalLineTo(17f)
    }
