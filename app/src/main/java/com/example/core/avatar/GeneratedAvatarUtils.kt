package com.example.core.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import java.io.ByteArrayOutputStream

enum class AvatarPresetStyle(
    val id: String,
    val titleAr: String,
    val startColorHex: Int,
    val endColorHex: Int,
    val textColorHex: Int
) {
    WARM_SUNSET("sunset", "غروب دافئ", 0xFFFF512F.toInt(), 0xFFDD2476.toInt(), 0xFFFFFFFF.toInt()),
    DEEP_OCEAN("ocean", "محيط عميق", 0xFF2193b0.toInt(), 0xFF6dd5ed.toInt(), 0xFFFFFFFF.toInt()),
    EMERALD_GLOW("emerald", "زمرد براق", 0xFF11998e.toInt(), 0xFF38ef7d.toInt(), 0xFFFFFFFF.toInt()),
    PURPLE_NEBULA("purple", "سديم بنفسجي", 0xFF8E2DE2.toInt(), 0xFF4A00E0.toInt(), 0xFFFFFFFF.toInt()),
    GOLDEN_HOUR("golden", "ساعة ذهبية", 0xFFF2994A.toInt(), 0xFFF2C94C.toInt(), 0xFF1A1A1A.toInt()),
    CYBER_PUNK("cyber", "سايبر نيون", 0xFF00F2FE.toInt(), 0xFF4FACFE.toInt(), 0xFF001A33.toInt())
}

object GeneratedAvatarUtils {

    fun generateAvatarBitmap(
        preset: AvatarPresetStyle,
        initials: String,
        sizePx: Int = 512
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Gradient Paint
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, sizePx.toFloat(), sizePx.toFloat(),
                preset.startColorHex, preset.endColorHex,
                Shader.TileMode.CLAMP
            )
        }

        // Outer Circle Background
        val center = sizePx / 2f
        canvas.drawCircle(center, center, center, bgPaint)

        // Subtle Decorative Inner Ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 40
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.03f
        }
        canvas.drawCircle(center, center, center * 0.88f, ringPaint)

        // Decorative Abstract Geometrics
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 25
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center * 0.4f, center * 0.4f, sizePx * 0.18f, shapePaint)
        canvas.drawCircle(center * 1.6f, center * 1.5f, sizePx * 0.22f, shapePaint)

        // Initials Text
        val cleanInitials = initials.trim().take(2).uppercase().ifEmpty { "A" }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = preset.textColorHex
            textSize = sizePx * 0.38f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val textY = center - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(cleanInitials, center, textY, textPaint)

        return bitmap
    }

    fun bitmapToJpegBytes(bitmap: Bitmap, quality: Int = 90): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    fun compressUriToJpegBytes(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int = 512,
        quality: Int = 85
    ): ByteArray? {
        return runCatching {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val (width, height) = options.outWidth to options.outHeight
            if (width <= 0 || height <= 0) return null

            var inSampleSize = 1
            if (height > maxDimensionPx || width > maxDimensionPx) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while ((halfHeight / inSampleSize) >= maxDimensionPx && (halfWidth / inSampleSize) >= maxDimensionPx) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val finalBitmap = scaleBitmapToMaxDimension(sampledBitmap, maxDimensionPx)
            val bytes = bitmapToJpegBytes(finalBitmap, quality)
            if (finalBitmap != sampledBitmap) {
                finalBitmap.recycle()
            }
            sampledBitmap.recycle()
            bytes
        }.getOrNull()
    }

    fun compressBitmapToJpegBytes(
        bitmap: Bitmap,
        maxDimensionPx: Int = 512,
        quality: Int = 85
    ): ByteArray {
        val scaled = scaleBitmapToMaxDimension(bitmap, maxDimensionPx)
        val bytes = bitmapToJpegBytes(scaled, quality)
        if (scaled != bitmap) {
            scaled.recycle()
        }
        return bytes
    }

    private fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDimensionPx: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimensionPx && height <= maxDimensionPx) {
            return bitmap
        }
        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width > height) {
            targetWidth = maxDimensionPx
            targetHeight = (maxDimensionPx / ratio).toInt().coerceAtLeast(1)
        } else {
            targetHeight = maxDimensionPx
            targetWidth = (maxDimensionPx * ratio).toInt().coerceAtLeast(1)
        }
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
