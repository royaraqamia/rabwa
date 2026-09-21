package com.example.domain.model.storage

data class UploadFileRequest(
    val bucket: String,
    val path: String,
    val data: ByteArray,
    val mimeType: String = "application/octet-stream",
    val upsert: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UploadFileRequest
        return bucket == other.bucket && path == other.path && data.contentEquals(other.data) && mimeType == other.mimeType && upsert == other.upsert
    }

    override fun hashCode(): Int {
        var result = bucket.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + upsert.hashCode()
        return result
    }
}
