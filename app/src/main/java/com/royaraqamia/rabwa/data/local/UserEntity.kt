package com.royaraqamia.rabwa.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String?,
    @ColumnInfo(name = "display_name")
    val displayName: String? = null,
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,
    @ColumnInfo(name = "is_email_confirmed")
    val isEmailConfirmed: Boolean = false,
    val provider: String = "UNKNOWN",
    @ColumnInfo(name = "created_at")
    val createdAt: Long? = null,
    @ColumnInfo(name = "last_cached_at")
    val lastCachedAt: Long = System.currentTimeMillis()
)
