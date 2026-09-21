package com.example.domain.model.qr

/**
 * Validated, sanitized domain model representing a scanned QR code result.
 */
data class QrScanResult(
    val rawContent: String,
    val sanitizedContent: String,
    val contentType: QrContentType,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isUrl: Boolean
        get() = contentType == QrContentType.URL || sanitizedContent.startsWith("http://") || sanitizedContent.startsWith("https://") || sanitizedContent.startsWith("www.")

    val actionUrl: String
        get() = when {
            sanitizedContent.startsWith("www.") -> "https://$sanitizedContent"
            !sanitizedContent.startsWith("http://") && !sanitizedContent.startsWith("https://") -> "https://$sanitizedContent"
            else -> sanitizedContent
        }

    val isPhone: Boolean
        get() = contentType == QrContentType.PHONE || sanitizedContent.startsWith("tel:")

    val isEmail: Boolean
        get() = contentType == QrContentType.EMAIL || sanitizedContent.startsWith("mailto:")

    val isGeo: Boolean
        get() = contentType == QrContentType.GEO || sanitizedContent.startsWith("geo:")
}

