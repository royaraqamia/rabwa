package com.example.domain.usecase

import com.example.core.result.AppResult
import com.example.domain.repository.ArchitectureRepository

class ClearRecordsUseCase(
    private val repository: ArchitectureRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.clearRecords()
    }
}
