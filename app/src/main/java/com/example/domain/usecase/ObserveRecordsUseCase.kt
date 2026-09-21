package com.example.domain.usecase

import com.example.domain.model.ArchitectureRecord
import com.example.domain.repository.ArchitectureRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecordsUseCase(
    private val repository: ArchitectureRepository
) {
    operator fun invoke(): Flow<List<ArchitectureRecord>> {
        return repository.observeRecords()
    }
}
