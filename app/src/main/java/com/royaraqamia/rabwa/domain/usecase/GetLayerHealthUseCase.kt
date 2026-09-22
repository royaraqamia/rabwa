package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.LayerHealth
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository

class GetLayerHealthUseCase(
    private val repository: ArchitectureRepository
) {
    suspend operator fun invoke(): AppResult<List<LayerHealth>> {
        return repository.getLayerHealth()
    }
}
