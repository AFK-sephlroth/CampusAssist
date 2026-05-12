package com.example.campusassist.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles all Firestore operations for service tickets.
 *
 * Firestore structure:
 *   service_tickets/{firestoreId}  — one document per ticket
 *
 * The document stores every field from ServiceTicketEntity plus:
 *   - firestoreId  (String)  — Firestore document ID
 *   - localId      (Long)    — Room primary key, used to match records on sync
 *   - createdBy    (String)  — Firebase UID of the submitter
 *
 * All authenticated users can read/write (matches our "everyone sees all" rule).
 */
class FirebaseTicketSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        const val COLLECTION = "service_tickets"
    }

    private val col get() = firestore.collection(COLLECTION)

    // ── Real-time listener ────────────────────────────────────────────────────

    /**
     * Emits the full ticket list from Firestore whenever any document changes.
     * The Flow stays active until cancelled; use it in a coroutine scope tied
     * to the repository lifetime.
     */
    fun observeAllTickets(): Flow<List<Map<String, Any?>>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val docs = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.also { it["firestoreId"] = doc.id }
                }
                trySend(docs)
            }
        awaitClose { registration?.remove() }
    }

    // ── Write operations ──────────────────────────────────────────────────────

    /**
     * Saves a new ticket to Firestore. Returns the generated Firestore document
     * ID so it can be stored in Room for future updates/deletes.
     */
    suspend fun createTicket(data: Map<String, Any?>): String {
        val ref = col.add(data + mapOf("createdBy" to (auth.currentUser?.uid ?: ""))).await()
        return ref.id
    }

    /**
     * Overwrites the Firestore document for [firestoreId] with [data].
     */
    suspend fun updateTicket(firestoreId: String, data: Map<String, Any?>) {
        col.document(firestoreId).set(data).await()
    }

    /**
     * Partially updates specific fields on a Firestore document.
     * Useful for status/notes changes without re-uploading attachments.
     */
    suspend fun patchTicket(firestoreId: String, fields: Map<String, Any?>) {
        col.document(firestoreId).update(fields).await()
    }

    /**
     * Deletes the Firestore document for [firestoreId].
     */
    suspend fun deleteTicket(firestoreId: String) {
        col.document(firestoreId).delete().await()
    }

    // ── One-time fetch (used during offline → online reconciliation) ──────────

    suspend fun fetchAllTickets(): List<Map<String, Any?>> {
        return try {
            col.orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
                .documents
                .mapNotNull { doc ->
                    doc.data?.toMutableMap()?.also { it["firestoreId"] = doc.id }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
