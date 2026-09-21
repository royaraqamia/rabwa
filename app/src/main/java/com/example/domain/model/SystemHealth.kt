package com.example.domain.model

enum class LayerTier {
    PRESENTATION,
    APPLICATION,
    DOMAIN,
    DATA
}

data class LayerHealth(
    val tier: LayerTier,
    val name: String,
    val isOperational: Boolean,
    val responsibilities: String,
    val technology: String
)

data class ArchitectureRecord(
    val id: String,
    val title: String,
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastSyncedAt: Long? = null
)
