package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecordsUseCase(
    private val repository: ArchitectureRepository
) {
    operator fun invoke(): Flow<List<ArchitectureRecord>> {
        return repository.observeRecords()
    }
}
