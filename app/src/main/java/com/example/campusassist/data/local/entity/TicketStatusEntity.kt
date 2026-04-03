package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket_statuses")
data class TicketStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,       // Pending, In Progress, Completed
    val colorHex: String    // e.g. "#FFA500"
)