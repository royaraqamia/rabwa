package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository

/**
 * UseCase orchestrating synchronization of offline-queued records with remote/authoritative storage.
 */
class SyncOfflineRecordsUseCase(
    private val repository: ArchitectureRepository
) {
    suspend operator fun invoke(): AppResult<Int> {
        return repository.synchronizePendingRecords()
    }
}
