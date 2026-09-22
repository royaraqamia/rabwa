package com.royaraqamia.rabwa.ui.icon

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lucide navigation icons for Shadcn UI styling.
 */
val LucideIcons.Home: ImageVector
    get() = lucideIcon("LucideHome") {
        // Path: M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z
        moveTo(3f, 9f)
        lineTo(12f, 2f)
        lineTo(21f, 9f)
        verticalLineTo(20f)
        curveTo(21f, 20.53f, 20.79f, 21.04f, 20.41f, 21.41f)
        curveTo(20.04f, 21.79f, 19.53f, 22f, 19f, 22f)
        horizontalLineTo(5f)
        curveTo(4.47f, 22f, 3.96f, 21.79f, 3.59f, 21.41f)
        curveTo(3.21f, 21.04f, 3f, 20.53f, 3f, 20f)
        close()
        // Path: M9 22V12h6v10
        moveTo(9f, 22f)
        verticalLineTo(12f)
        horizontalLineTo(15f)
        verticalLineTo(22f)
    }

val LucideIcons.User: ImageVector
    get() = lucideIcon("LucideUser") {
        // Path: M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2
        moveTo(19f, 21f)
        verticalLineTo(19f)
        curveTo(19f, 17.94f, 18.58f, 16.92f, 17.83f, 16.17f)
        curveTo(17.08f, 15.42f, 16.06f, 15f, 15f, 15f)
        horizontalLineTo(9f)
        curveTo(7.94f, 15f, 6.92f, 15.42f, 6.17f, 16.17f)
        curveTo(5.42f, 16.92f, 5f, 17.94f, 5f, 19f)
        verticalLineTo(21f)
        // Path: circle cx=12 cy=7 r=4
        moveTo(16f, 7f)
        curveTo(16f, 9.21f, 14.21f, 11f, 12f, 11f)
        curveTo(9.79f, 11f, 8f, 9.21f, 8f, 7f)
        curveTo(8f, 4.79f, 9.79f, 3f, 12f, 3f)
        curveTo(14.21f, 3f, 16f, 4.79f, 16f, 7f)
        close()
    }

val LucideIcons.UserPlus: ImageVector
    get() = lucideIcon("LucideUserPlus") {
        // Path: M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2
        moveTo(16f, 21f)
        verticalLineTo(19f)
        curveTo(16f, 17.94f, 15.58f, 16.92f, 14.83f, 16.17f)
        curveTo(14.08f, 15.42f, 13.06f, 15f, 12f, 15f)
        horizontalLineTo(6f)
        curveTo(4.94f, 15f, 3.92f, 15.42f, 3.17f, 16.17f)
        curveTo(2.42f, 16.92f, 2f, 17.94f, 2f, 19f)
        verticalLineTo(21f)
        // circle cx=9 cy=7 r=4
        moveTo(13f, 7f)
        curveTo(13f, 9.21f, 11.21f, 11f, 9f, 11f)
        curveTo(6.79f, 11f, 5f, 9.21f, 5f, 7f)
        curveTo(5f, 4.79f, 6.79f, 3f, 9f, 3f)
        curveTo(11.21f, 3f, 13f, 4.79f, 13f, 7f)
        close()
        // line x1=19 y1=8 x2=19 y2=14
        moveTo(19f, 8f)
        verticalLineTo(14f)
        // line x1=22 y1=11 x2=16 y2=11
        moveTo(22f, 11f)
        horizontalLineTo(16f)
    }

val LucideIcons.LogIn: ImageVector
    get() = lucideIcon("LucideLogIn") {
        // Path: M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4
        moveTo(15f, 3f)
        horizontalLineTo(19f)
        curveTo(19.53f, 3f, 20.04f, 3.21f, 20.41f, 3.59f)
        curveTo(20.79f, 3.96f, 21f, 4.47f, 21f, 5f)
        verticalLineTo(19f)
        curveTo(21f, 19.53f, 20.79f, 20.04f, 20.41f, 20.41f)
        curveTo(20.04f, 20.79f, 19.53f, 21f, 19f, 21f)
        horizontalLineTo(15f)
        // polyline points: 10 17 15 12 10 7
        moveTo(10f, 17f)
        lineTo(15f, 12f)
        lineTo(10f, 7f)
        // line x1=15 y1=12 x2=3 y2=12
        moveTo(15f, 12f)
        horizontalLineTo(3f)
    }

val LucideIcons.LogOut: ImageVector
    get() = lucideIcon("LucideLogOut") {
        // Path: M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4
        moveTo(9f, 21f)
        horizontalLineTo(5f)
        curveTo(4.47f, 21f, 3.96f, 20.79f, 3.59f, 20.41f)
        curveTo(3.21f, 20.04f, 3f, 19.53f, 3f, 19f)
        verticalLineTo(5f)
        curveTo(3f, 4.47f, 3.21f, 3.96f, 3.59f, 3.59f)
        curveTo(3.96f, 3.21f, 4.47f, 3f, 5f, 3f)
        horizontalLineTo(9f)
        // polyline points: 16 17 21 12 16 7
        moveTo(16f, 17f)
        lineTo(21f, 12f)
        lineTo(16f, 7f)
        // line x1=21 y1=12 x2=9 y2=12
        moveTo(21f, 12f)
        horizontalLineTo(9f)
    }

val LucideIcons.ArrowLeft: ImageVector
    get() = lucideIcon("LucideArrowLeft") {
        // line x1=19 y1=12 x2=5 y2=12
        moveTo(19f, 12f)
        horizontalLineTo(5f)
        // polyline points: 12 19 5 12 12 5
        moveTo(12f, 19f)
        lineTo(5f, 12f)
        lineTo(12f, 5f)
    }
