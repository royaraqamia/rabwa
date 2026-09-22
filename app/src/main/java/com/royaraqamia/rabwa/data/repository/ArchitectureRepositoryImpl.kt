package com.royaraqamia.rabwa.data.repository

import com.royaraqamia.rabwa.core.dispatcher.CoroutineDispatchers
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.data.local.ArchitectureDao
import com.royaraqamia.rabwa.data.mapper.toDomain
import com.royaraqamia.rabwa.data.mapper.toEntity
import com.royaraqamia.rabwa.domain.model.ArchitectureRecord
import com.royaraqamia.rabwa.domain.model.LayerHealth
import com.royaraqamia.rabwa.domain.model.LayerTier
import com.royaraqamia.rabwa.domain.model.SyncStatus
import com.royaraqamia.rabwa.domain.model.SyncSummary
import com.royaraqamia.rabwa.domain.network.ConnectivityMonitor
import com.royaraqamia.rabwa.domain.repository.ArchitectureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ArchitectureRepositoryImpl(
    private val dao: ArchitectureDao,
    private val dispatchers: CoroutineDispatchers,
    private val connectivityMonitor: ConnectivityMonitor? = null
) : ArchitectureRepository {

    override suspend fun getLayerHealth(): AppResult<List<LayerHealth>> = withContext(dispatchers.io) {
        AppResult.runCatching {
            listOf(
                LayerHealth(
                    tier = LayerTier.PRESENTATION,
                    name = "طبقة العرض والواجهة",
                    isOperational = true,
                    responsibilities = "مكونات Jetpack Compose، نظام Material 3، ومؤشرات المزامنة دون اتصال",
                    technology = "Jetpack Compose + Material 3"
                ),
                LayerHealth(
                    tier = LayerTier.APPLICATION,
                    name = "طبقة التطبيق والتنسيق",
                    isOperational = true,
                    responsibilities = "إدارة الحالة (StateFlow)، المزامنة التلقائية، ومرونة الاتصال دون أخطاء حظر",
                    technology = "Lifecycle ViewModel + StateFlow"
                ),
                LayerHealth(
                    tier = LayerTier.DOMAIN,
                    name = "طبقة النطاق وقواعد الأعمال",
                    isOperational = true,
                    responsibilities = "حالات الاستخدام (UseCases)، تدقيق المدخلات (Zero-Trust)، ونماذج المزامنة النقية",
                    technology = "Pure Kotlin Core"
                ),
                LayerHealth(
                    tier = LayerTier.DATA,
                    name = "طبقة البيانات والتخزين (Offline-First SSOT)",
                    isOperational = true,
                    responsibilities = "قاعدة بيانات Room كمصدر وحيد للحقيقة، وقوائم الانتظار للمزامنة",
                    technology = "Room Database + Flow"
                )
            )
        }
    }

    override suspend fun addRecord(sanitizedTitle: String): AppResult<ArchitectureRecord> = withContext(dispatchers.io) {
        try {
            val isOnline = connectivityMonitor?.isCurrentlyConnected ?: true
            val now = System.currentTimeMillis()
            val syncStatus = if (isOnline) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC
            val lastSyncedAt = if (isOnline) now else null

            val record = ArchitectureRecord(
                id = UUID.randomUUID().toString(),
                title = sanitizedTitle,
                timestamp = now,
                syncStatus = syncStatus,
                lastSyncedAt = lastSyncedAt
            )
            dao.insert(record.toEntity())
            AppResult.Success(record)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Database("فشل حفظ السجل في قاعدة البيانات: ${e.message}"))
        }
    }

    override fun observeRecords(): Flow<List<ArchitectureRecord>> {
        return dao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun observeSyncSummary(): Flow<SyncSummary> {
        return combine(
            dao.observePendingSyncCount(),
            dao.observeSyncedCount()
        ) { pending, synced ->
            SyncSummary(
                pendingCount = pending,
                syncedCount = synced,
                lastSyncedAt = if (pending == 0 && synced > 0) System.currentTimeMillis() else null,
                isSyncing = false
            )
        }.flowOn(dispatchers.io)
    }

    override suspend fun synchronizePendingRecords(): AppResult<Int> = withContext(dispatchers.io) {
        try {
            val pending = dao.getPendingSyncRecords()
            if (pending.isEmpty()) {
                return@withContext AppResult.Success(0)
            }
            val ids = pending.map { it.id }
            val syncTimestamp = System.currentTimeMillis()
            dao.updateSyncStatus(ids = ids, status = SyncStatus.SYNCED.name, syncedAt = syncTimestamp)
            AppResult.Success(ids.size)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Database("فشل تنفيذ مزامنة البيانات دون اتصال: ${e.message}"))
        }
    }

    override suspend fun clearRecords(): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            dao.clearAll()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Database("فشل مسح السجلات: ${e.message}"))
        }
    }
}
