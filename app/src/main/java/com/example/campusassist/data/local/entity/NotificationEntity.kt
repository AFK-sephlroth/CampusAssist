package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val ticketId: Long = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)