package com.example.domain.model.qr

/**
 * Categorization of parsed QR code payloads for smart contextual actions.
 */
enum class QrContentType {
    URL,
    TEXT,
    WIFI,
    EMAIL,
    PHONE,
    GEO,
    UNKNOWN
}
