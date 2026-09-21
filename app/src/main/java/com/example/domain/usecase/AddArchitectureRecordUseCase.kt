package com.example.domain.usecase

import com.example.core.result.AppResult
import com.example.core.validation.InputValidator
import com.example.domain.model.ArchitectureRecord
import com.example.domain.repository.ArchitectureRepository

/**
 * UseCase executing Zero-Trust validation before delegating persistence to the repository.
 */
class AddArchitectureRecordUseCase(
    private val repository: ArchitectureRepository
) {
    suspend operator fun invoke(rawTitle: String): AppResult<ArchitectureRecord> {
        val validationResult = InputValidator.validateLength(
            field = "Record Title",
            value = rawTitle,
            minLength = 2,
            maxLength = 60
        )

        val sanitizedTitle = when (validationResult) {
            is AppResult.Failure -> return validationResult
            is AppResult.Success -> validationResult.data
        }

        return repository.addRecord(sanitizedTitle)
    }
}
