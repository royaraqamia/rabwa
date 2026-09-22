package com.royaraqamia.rabwa.domain.model

/**
 * Domain-level summary of the application's offline-first synchronization health.
 */
data class SyncSummary(
    val pendingCount: Int = 0,
    val syncedCount: Int = 0,
    val lastSyncedAt: Long? = null,
    val isSyncing: Boolean = false
) {
    val isFullySynced: Boolean
        get() = pendingCount == 0 && !isSyncing
}
