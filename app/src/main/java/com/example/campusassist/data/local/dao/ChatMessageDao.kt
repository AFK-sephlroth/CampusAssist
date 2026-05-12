package com.example.campusassist.data.local.dao

import androidx.room.*
import com.example.campusassist.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages WHERE ticketId = :ticketId ORDER BY sentAt ASC")
    fun getMessagesForTicket(ticketId: Long): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE ticketId = :ticketId")
    suspend fun deleteMessagesForTicket(ticketId: Long)
}
