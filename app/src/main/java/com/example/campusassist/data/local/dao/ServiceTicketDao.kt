package com.example.campusassist.data.local.dao

import androidx.room.*
import com.example.campusassist.data.local.entity.ServiceTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceTicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: ServiceTicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<ServiceTicketEntity>)

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

    @Query("SELECT * FROM service_tickets WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getTicketByFirestoreId(firestoreId: String): ServiceTicketEntity?

    @Query("SELECT * FROM service_tickets WHERE isSynced = 0")
    suspend fun getUnsyncedTickets(): List<ServiceTicketEntity>

    @Query("UPDATE service_tickets SET isSynced = 1, firestoreId = :firestoreId WHERE id = :id")
    suspend fun markAsSynced(id: Long, firestoreId: String)

    @Query("UPDATE service_tickets SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE service_tickets SET status = :status, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long)

    @Query("UPDATE service_tickets SET notes = :notes, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?, updatedAt: Long)

    @Query("DELETE FROM service_tickets WHERE firestoreId = :firestoreId")
    suspend fun deleteByFirestoreId(firestoreId: String)

    @Query("DELETE FROM service_tickets")
    suspend fun clearAll()
}
