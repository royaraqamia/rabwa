package com.example.domain.model.sync

data class RemoteSyncResult(
    val totalProcessed: Int,
    val successCount: Int,
    val failedCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
