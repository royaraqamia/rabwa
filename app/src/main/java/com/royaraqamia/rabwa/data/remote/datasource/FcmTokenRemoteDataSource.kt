package com.royaraqamia.rabwa.data.remote.datasource

import com.royaraqamia.rabwa.data.remote.dto.SupabaseFcmTokenDto

/**
 * Contract for remote FCM token persistence in Supabase.
 */
interface FcmTokenRemoteDataSource {
    suspend fun upsertToken(tokenDto: SupabaseFcmTokenDto)
    suspend fun deleteToken(token: String)
    suspend fun deleteTokensForUser(userId: String)
}
