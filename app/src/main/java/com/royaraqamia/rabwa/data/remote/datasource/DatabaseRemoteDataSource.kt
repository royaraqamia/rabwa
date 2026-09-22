package com.royaraqamia.rabwa.data.remote.datasource

import com.royaraqamia.rabwa.core.network.SupabaseClientProvider
import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import com.royaraqamia.rabwa.data.remote.dto.SupabaseRecordDto
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

/**
 * Interface isolating PostgREST table interactions from business logic.
 */
interface DatabaseRemoteDataSource {
    suspend fun fetchRecords(tableName: String = "architecture_records"): AppResult<List<SupabaseRecordDto>>
    suspend fun upsertRecord(record: SupabaseRecordDto, tableName: String = "architecture_records"): AppResult<SupabaseRecordDto>
    suspend fun bulkUpsert(records: List<SupabaseRecordDto>, tableName: String = "architecture_records"): AppResult<Unit>
    suspend fun deleteRecord(id: String, tableName: String = "architecture_records"): AppResult<Unit>
}

class SupabaseDatabaseRemoteDataSource(
    private val postgrestProvider: () -> Postgrest = { SupabaseClientProvider.postgrest }
) : DatabaseRemoteDataSource {

    private val postgrest: Postgrest get() = postgrestProvider()

    override suspend fun fetchRecords(tableName: String): AppResult<List<SupabaseRecordDto>> {
        return runCatching {
            postgrest.from(tableName).select().decodeList<SupabaseRecordDto>()
        }.fold(
            onSuccess = { list -> AppResult.Success(list) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.RemoteDatabase(
                        details = throwable.localizedMessage,
                        message = "Failed to fetch records from table '$tableName'"
                    )
                )
            }
        )
    }

    override suspend fun upsertRecord(
        record: SupabaseRecordDto,
        tableName: String
    ): AppResult<SupabaseRecordDto> {
        return runCatching {
            postgrest.from(tableName).upsert(record)
            record
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.RemoteDatabase(
                        details = throwable.localizedMessage,
                        message = "Failed to upsert record '${record.id}' into table '$tableName'"
                    )
                )
            }
        )
    }

    override suspend fun bulkUpsert(
        records: List<SupabaseRecordDto>,
        tableName: String
    ): AppResult<Unit> {
        if (records.isEmpty()) return AppResult.Success(Unit)
        return runCatching {
            postgrest.from(tableName).upsert(records)
            Unit
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.RemoteDatabase(
                        details = throwable.localizedMessage,
                        message = "Failed to bulk upsert ${records.size} records into table '$tableName'"
                    )
                )
            }
        )
    }

    override suspend fun deleteRecord(id: String, tableName: String): AppResult<Unit> {
        return runCatching {
            postgrest.from(tableName).delete {
                filter {
                    eq("id", id)
                }
            }
            Unit
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable ->
                AppResult.Failure(
                    AppError.RemoteDatabase(
                        details = throwable.localizedMessage,
                        message = "Failed to delete record '$id' from table '$tableName'"
                    )
                )
            }
        )
    }
}
