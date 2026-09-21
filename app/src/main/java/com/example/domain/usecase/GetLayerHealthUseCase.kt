package com.example.domain.usecase

import com.example.core.result.AppResult
import com.example.domain.model.LayerHealth
import com.example.domain.repository.ArchitectureRepository

class GetLayerHealthUseCase(
    private val repository: ArchitectureRepository
) {
    suspend operator fun invoke(): AppResult<List<LayerHealth>> {
        return repository.getLayerHealth()
    }
}
