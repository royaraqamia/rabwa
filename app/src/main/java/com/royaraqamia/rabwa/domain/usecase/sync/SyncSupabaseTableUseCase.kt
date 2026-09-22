package com.royaraqamia.rabwa.domain.usecase.sync

import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.domain.model.sync.RemoteSyncResult
import com.royaraqamia.rabwa.domain.repository.RemoteSyncRepository

class SyncSupabaseTableUseCase(
    private val remoteSyncRepository: RemoteSyncRepository
) {
    suspend operator fun invoke(): AppResult<RemoteSyncResult> {
        return remoteSyncRepository.syncPendingRecords()
    }
}
