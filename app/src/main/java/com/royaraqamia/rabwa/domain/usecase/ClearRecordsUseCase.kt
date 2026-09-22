package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository

class ClearRecordsUseCase(
    private val repository: ArchitectureRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.clearRecords()
    }
}
