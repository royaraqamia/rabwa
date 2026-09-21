package com.example.data.remote.datasource

import com.example.core.network.SupabaseClientProvider
import com.example.data.remote.dto.SupabaseFcmTokenDto
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

/**
 * Production implementation of FcmTokenRemoteDataSource using Supabase PostgREST.
 */
class SupabaseFcmTokenRemoteDataSource(
    private val postgrestProvider: () -> Postgrest = { SupabaseClientProvider.postgrest }
) : FcmTokenRemoteDataSource {

    private val postgrest: Postgrest get() = postgrestProvider()

    override suspend fun upsertToken(tokenDto: SupabaseFcmTokenDto) {
        postgrest.from(TABLE_NAME).upsert(tokenDto)
    }

    override suspend fun deleteToken(token: String) {
        postgrest.from(TABLE_NAME).delete {
            filter {
                eq("token", token)
            }
        }
    }

    override suspend fun deleteTokensForUser(userId: String) {
        postgrest.from(TABLE_NAME).delete {
            filter {
                eq("user_id", userId)
            }
        }
    }

    companion object {
        const val TABLE_NAME = "user_fcm_tokens"
    }
}
