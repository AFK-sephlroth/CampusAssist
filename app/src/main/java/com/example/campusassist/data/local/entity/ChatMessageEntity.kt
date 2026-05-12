package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ServiceTicketEntity::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ticketId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketId: Long,
    val senderUsername: String,
    val senderDisplayName: String,
    val message: String,
    val sentAt: Long = System.currentTimeMillis()
)
