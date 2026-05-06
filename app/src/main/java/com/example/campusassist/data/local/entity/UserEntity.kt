package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val fullname: String,
    val department: String?,
    val role: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
