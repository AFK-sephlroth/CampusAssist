package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.ServiceTicketDao
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.data.mapper.toFirestoreMap
import com.example.campusassist.data.mapper.toServiceTicketEntity
import com.example.campusassist.data.remote.FirebaseTicketSource
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketStatus
import com.example.campusassist.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val dao: ServiceTicketDao,
    private val firestore: FirebaseTicketSource
) : TicketRepository {

    // ── Read (Room is the UI source of truth) ─────────────────────────────────

    override fun getAllTickets(): Flow<List<ServiceTicket>> =
        dao.getAllTickets().map { list -> list.map { it.toDomain() } }

    override fun getTicketsByStatus(status: TicketStatus): Flow<List<ServiceTicket>> =
        dao.getTicketsByStatus(status.displayName).map { list -> list.map { it.toDomain() } }

    override suspend fun getTicketById(id: Long): ServiceTicket? =
        dao.getTicketById(id)?.toDomain()

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * 1. Insert into Room immediately (offline-first, UI updates instantly).
     * 2. Push to Firestore.
     * 3. Update Room record with the Firestore document ID + mark as synced.
     *
     * If Firestore push fails (offline), the ticket stays in Room with
     * isSynced = false and will be pushed next time syncUnsyncedTickets() runs.
     */
    override suspend fun createTicket(ticket: ServiceTicket): Long {
        // Step 1 — Room insert
        val localId = dao.insertTicket(ticket.toEntity())

        // Step 2 — Firestore push (best-effort)
        try {
            val firestoreId = firestore.createTicket(
                ticket.copy(id = localId).toFirestoreMap()
            )
            // Step 3 — Store Firestore ID in Room
            dao.markAsSynced(localId, firestoreId)
        } catch (e: Exception) {
            // Offline — will sync later, isSynced stays false
        }

        return localId
    }

    // ── Update ────────────────────────────────────────────────────────────────

    override suspend fun updateTicket(ticket: ServiceTicket) {
        dao.updateTicket(ticket.toEntity())
        ticket.firestoreId?.let {
            try { firestore.updateTicket(it, ticket.toFirestoreMap()) } catch (_: Exception) {}
        }
    }

    override suspend fun updateStatus(id: Long, status: TicketStatus) {
        val now = System.currentTimeMillis()
        dao.updateStatus(id, status.displayName, now)

        // Patch only the changed fields on Firestore — avoids re-uploading attachments
        val firestoreId = dao.getTicketById(id)?.firestoreId
        firestoreId?.let {
            try {
                firestore.patchTicket(it, mapOf("status" to status.displayName, "updatedAt" to now))
                dao.markAsSynced(id, it)
            } catch (_: Exception) {}
        }
    }

    override suspend fun updateNotes(id: Long, notes: String?) {
        val now = System.currentTimeMillis()
        dao.updateNotes(id, notes, now)

        val firestoreId = dao.getTicketById(id)?.firestoreId
        firestoreId?.let {
            try {
                firestore.patchTicket(it, mapOf("notes" to notes, "updatedAt" to now))
                dao.markAsSynced(id, it)
            } catch (_: Exception) {}
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    override suspend fun deleteTicket(ticket: ServiceTicket) {
        dao.deleteTicket(ticket.toEntity())
        ticket.firestoreId?.let {
            try { firestore.deleteTicket(it) } catch (_: Exception) {}
        }
    }

    // ── Sync helpers ──────────────────────────────────────────────────────────

    override suspend fun getUnsyncedTickets(): List<ServiceTicket> =
        dao.getUnsyncedTickets().map { it.toDomain() }

    override suspend fun markAsSynced(id: Long, firestoreId: String) =
        dao.markAsSynced(id, firestoreId)

    /**
     * Pulls all tickets from Firestore and upserts them into Room.
     * Called on login or when the device comes back online.
     * Existing Room records are matched by firestoreId and replaced.
     */
    override suspend fun syncFromFirestore() {
        try {
            val remoteDocs = firestore.fetchAllTickets()
            val entities   = remoteDocs.map { it.toServiceTicketEntity() }
            if (entities.isNotEmpty()) {
                dao.insertTickets(entities)   // REPLACE strategy handles upserts
            }
        } catch (_: Exception) {
            // Silently fail — Room data remains as-is
        }
    }

    /**
     * Pushes any locally-created tickets that failed to sync (e.g. created
     * while offline). Called by SyncViewModel when connectivity is restored.
     */
    suspend fun syncUnsyncedTickets() {
        val unsynced = dao.getUnsyncedTickets()
        unsynced.forEach { entity ->
            try {
                if (entity.firestoreId == null) {
                    // Never reached Firestore — create it
                    val firestoreId = firestore.createTicket(entity.toDomain().toFirestoreMap())
                    dao.markAsSynced(entity.id, firestoreId)
                } else {
                    // Exists in Firestore but local edits not pushed yet
                    firestore.updateTicket(entity.firestoreId, entity.toDomain().toFirestoreMap())
                    dao.markAsSynced(entity.id, entity.firestoreId)
                }
            } catch (_: Exception) {
                // Still offline — leave for next attempt
            }
        }
    }
}
