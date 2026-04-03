package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,                  // e.g. student ID "2024-00123"
    val name: String,
    val email: String,
    val department: String,
    val contactNumber: String,
    val role: String,                // STUDENT, STAFF, ADMIN
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)