package com.royaraqamia.rabwa.domain.model

/**
 * Represents the offline-first synchronization state of a domain entity.
 */
enum class SyncStatus {
    /**
     * Entity has been fully synchronized with remote/authoritative storage.
     */
    SYNCED,

    /**
     * Entity was created or mutated offline and is awaiting upstream synchronization.
     */
    PENDING_SYNC,

    /**
     * Entity is strictly stored locally on device and excluded from remote synchronization.
     */
    LOCAL_ONLY
}
