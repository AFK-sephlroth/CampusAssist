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
    val sentAt: Long = System.currentTimeMillis(),
    /** Firestore document ID for this message. Used to deduplicate rows when
     *  the real-time listener re-emits snapshots. Null for messages that were
     *  created offline before the ticket was synced to Firestore. */
    val firestoreId: String? = null
)
