package com.example.campusassist.domain.repository

import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketStatus
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun getAllTickets(): Flow<List<ServiceTicket>>
    fun getTicketsByStatus(status: TicketStatus): Flow<List<ServiceTicket>>
    suspend fun getTicketById(id: Long): ServiceTicket?
    suspend fun createTicket(ticket: ServiceTicket): Long
    suspend fun updateTicket(ticket: ServiceTicket)
    suspend fun updateStatus(id: Long, status: TicketStatus)
    suspend fun updateNotes(id: Long, notes: String?)
    suspend fun deleteTicket(ticket: ServiceTicket)
    suspend fun getUnsyncedTickets(): List<ServiceTicket>
    suspend fun markAsSynced(id: Long)
}
