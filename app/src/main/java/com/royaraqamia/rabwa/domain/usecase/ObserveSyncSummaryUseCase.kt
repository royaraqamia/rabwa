package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.domain.model.SyncSummary
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository
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
