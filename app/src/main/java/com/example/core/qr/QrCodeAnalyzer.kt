package com.example.core.qr

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * High-performance CameraX ImageAnalysis.Analyzer for real-time QR code frame detection using ZXing.
 * Configured with dedicated hints (QR format, UTF-8, try harder) for optimal scanning speed & precision.
 */
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
        )
    }

    @Volatile
    private var isScanningEnabled = true

    fun setScanningEnabled(enabled: Boolean) {
        isScanningEnabled = enabled
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanningEnabled) {
            imageProxy.close()
            return
        }

        try {
            val plane = imageProxy.planes[0]
            val buffer: ByteBuffer = plane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = imageProxy.width
            val height = imageProxy.height
            val rowStride = plane.rowStride

            val source = PlanarYUVLuminanceSource(
                data,
                rowStride,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decodeWithState(binaryBitmap)
                val qrText = result.text
                if (!qrText.isNullOrBlank() && isScanningEnabled) {
                    onQrCodeScanned(qrText)
                }
            } catch (_: NotFoundException) {
                // Try rotating bitmap if initial decode missed due to phone orientation
                try {
                    val rotatedSource = source.rotateCounterClockwise()
                    val rotatedBitmap = BinaryBitmap(HybridBinarizer(rotatedSource))
                    val result = reader.decodeWithState(rotatedBitmap)
                    val qrText = result.text
                    if (!qrText.isNullOrBlank() && isScanningEnabled) {
                        onQrCodeScanned(qrText)
                    }
                } catch (_: Exception) {
                    // Ignored - frame did not contain readable QR
                }
            } catch (_: Exception) {
                // Ignore decoding anomalies
            } finally {
                reader.reset()
            }
        } catch (_: Exception) {
            // Protect analyzer from unhandled image buffer exceptions
        } finally {
            imageProxy.close()
        }
    }
}

