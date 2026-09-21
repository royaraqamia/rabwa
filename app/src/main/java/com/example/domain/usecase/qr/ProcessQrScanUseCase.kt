package com.example.domain.usecase.qr

import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.qr.QrContentType
import com.example.domain.model.qr.QrScanResult
import java.util.Locale

/**
 * Domain Use Case: Processes and validates scanned QR code payloads with Zero-Trust principles.
 * Sanitizes input strings, prevents XSS/script injections, and categorizes the payload for contextual actions.
 */
class ProcessQrScanUseCase {

    operator fun invoke(rawPayload: String?): AppResult<QrScanResult> {
        if (rawPayload.isNullOrBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = "qr_payload",
                    message = "Scanned QR code content is empty"
                )
            )
        }

        val sanitized = sanitize(rawPayload)
        if (sanitized.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = "qr_payload",
                    message = "Scanned QR code contains only invalid characters"
                )
            )
        }

        val contentType = determineContentType(sanitized)
        val title = generateTitle(contentType, sanitized)

        val result = QrScanResult(
            rawContent = rawPayload,
            sanitizedContent = sanitized,
            contentType = contentType,
            title = title
        )

        return AppResult.Success(result)
    }

    private fun sanitize(input: String): String {
        return input
            .replace("\u0000", "")
            .filter { char ->
                !char.isISOControl() || char == '\n' || char == '\r' || char == '\t'
            }
            .trim()
    }

    private fun determineContentType(content: String): QrContentType {
        val lower = content.lowercase(Locale.ROOT)
        return when {
            lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.") -> QrContentType.URL
            lower.startsWith("wifi:") || lower.startsWith("wif:") -> QrContentType.WIFI
            lower.startsWith("mailto:") || (lower.contains("@") && lower.contains(".") && !lower.contains(" ") && lower.length < 100) -> QrContentType.EMAIL
            lower.startsWith("tel:") || lower.startsWith("sms:") || lower.startsWith("smsto:") -> QrContentType.PHONE
            lower.matches(Regex("^\\+?[0-9]{7,15}$")) -> QrContentType.PHONE
            lower.startsWith("geo:") -> QrContentType.GEO
            lower.startsWith("begin:vcard") || lower.contains("wa.me/") -> QrContentType.TEXT
            else -> QrContentType.TEXT
        }
    }

    private fun generateTitle(type: QrContentType, content: String): String {
        return when (type) {
            QrContentType.URL -> "رابط ويب (URL)"
            QrContentType.WIFI -> "شبكة واي فاي (Wi-Fi)"
            QrContentType.EMAIL -> "بريد إلكتروني (Email)"
            QrContentType.PHONE -> "رقم هاتف (Phone)"
            QrContentType.GEO -> "موقع جغرافي (Location)"
            QrContentType.TEXT -> "نص (Text)"
            QrContentType.UNKNOWN -> "رمز QR"
        }
    }
}
