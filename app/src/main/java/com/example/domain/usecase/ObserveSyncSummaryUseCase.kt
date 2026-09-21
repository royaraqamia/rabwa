package com.example.domain.usecase

import com.example.domain.model.SyncSummary
import com.example.domain.repository.ArchitectureRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase observing reactive synchronization health and pending record count.
 */
class ObserveSyncSummaryUseCase(
    private val repository: ArchitectureRepository
) {
    operator fun invoke(): Flow<SyncSummary> {
        return repository.observeSyncSummary()
    }
}
