package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM cached_users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM cached_users ORDER BY last_cached_at DESC LIMIT 1")
    fun observeLatestUser(): Flow<UserEntity?>

    @Query("SELECT * FROM cached_users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM cached_users ORDER BY last_cached_at DESC LIMIT 1")
    suspend fun getLatestUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM cached_users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM cached_users")
    suspend fun clearAllUsers()
}
