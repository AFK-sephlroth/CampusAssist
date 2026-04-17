package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,           // username
    val name: String,         // full name
    val department: String?,  // null for USER, department name for STAFF
    val role: String,         // "USER" or "STAFF"
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)