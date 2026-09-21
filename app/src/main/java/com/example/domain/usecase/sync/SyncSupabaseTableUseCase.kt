package com.example.domain.usecase.sync

import com.example.core.result.AppResult
import com.example.domain.model.sync.RemoteSyncResult
import com.example.domain.repository.RemoteSyncRepository

class SyncSupabaseTableUseCase(
    private val remoteSyncRepository: RemoteSyncRepository
) {
    suspend operator fun invoke(): AppResult<RemoteSyncResult> {
        return remoteSyncRepository.syncPendingRecords()
    }
}
