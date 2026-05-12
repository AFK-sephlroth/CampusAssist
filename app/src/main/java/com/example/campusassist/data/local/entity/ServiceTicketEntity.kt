package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_tickets")
data class ServiceTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String = "Pending",
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val departmentId: Long? = null,
    val notes: String? = null,
    val attachmentUris: String? = null,
    // Firestore document ID — null until the ticket has been synced at least once
    val firestoreId: String? = null,
    // Username of the person who created the ticket
    val createdBy: String? = null
)
