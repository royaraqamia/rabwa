package com.example.domain.usecase

import com.example.core.result.AppResult
import com.example.domain.repository.ArchitectureRepository

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
