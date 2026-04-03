package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.ServiceTicketDao
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketStatus
import com.example.campusassist.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val dao: ServiceTicketDao
) : TicketRepository {

    override fun getAllTickets(): Flow<List<ServiceTicket>> =
        dao.getAllTickets().map { list -> list.map { it.toDomain() } }

    override fun getTicketsByStatus(status: TicketStatus): Flow<List<ServiceTicket>> =
        dao.getTicketsByStatus(status.displayName).map { list -> list.map { it.toDomain() } }

    override suspend fun getTicketById(id: Long): ServiceTicket? =
        dao.getTicketById(id)?.toDomain()

    override suspend fun createTicket(ticket: ServiceTicket): Long =
        dao.insertTicket(ticket.toEntity())

    override suspend fun updateTicket(ticket: ServiceTicket) =
        dao.updateTicket(ticket.toEntity())

    override suspend fun deleteTicket(ticket: ServiceTicket) =
        dao.deleteTicket(ticket.toEntity())

    override suspend fun getUnsyncedTickets(): List<ServiceTicket> =
        dao.getUnsyncedTickets().map { it.toDomain() }

    override suspend fun markAsSynced(id: Long) =
        dao.markAsSynced(id)
}