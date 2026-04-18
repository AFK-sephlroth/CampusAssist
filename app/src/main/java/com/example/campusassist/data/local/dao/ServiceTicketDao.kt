package com.example.campusassist.data.local.dao

import androidx.room.*
import com.example.campusassist.data.local.entity.ServiceTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceTicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: ServiceTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: ServiceTicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: ServiceTicketEntity)

    @Query("SELECT * FROM service_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<ServiceTicketEntity>>

    @Query("SELECT * FROM service_tickets WHERE status = :status ORDER BY createdAt DESC")
    fun getTicketsByStatus(status: String): Flow<List<ServiceTicketEntity>>

    @Query("SELECT * FROM service_tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): ServiceTicketEntity?

    @Query("SELECT * FROM service_tickets WHERE isSynced = 0")
    suspend fun getUnsyncedTickets(): List<ServiceTicketEntity>

    @Query("UPDATE service_tickets SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE service_tickets SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long)

    @Query("UPDATE service_tickets SET notes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?, updatedAt: Long)
}
