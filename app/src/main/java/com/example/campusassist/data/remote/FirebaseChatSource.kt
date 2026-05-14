package com.example.campusassist.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore structure for chat messages:
 *
 *   service_tickets/{firestoreId}/messages/{messageId}
 *     - id               String   (Firestore document ID, echoed for easy mapping)
 *     - ticketFirestoreId String  (parent ticket's Firestore ID)
 *     - senderUsername   String
 *     - senderDisplayName String
 *     - message          String
 *     - sentAt           Long    (epoch millis)
 *
 * Using a subcollection keeps messages scoped to their ticket and lets
 * Firestore security rules mirror the parent ticket's permissions.
 */
@Singleton
class FirebaseChatSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TICKETS_COLLECTION  = "service_tickets"
        private const val MESSAGES_SUBCOLLECTION = "messages"
    }

    private fun messagesCol(ticketFirestoreId: String) =
        firestore.collection(TICKETS_COLLECTION)
            .document(ticketFirestoreId)
            .collection(MESSAGES_SUBCOLLECTION)

    // ── Real-time listener ────────────────────────────────────────────────────

    /**
     * Returns a Flow that emits the full message list for [ticketFirestoreId]
     * every time a message is added, updated, or deleted in Firestore.
     * This is what makes chat appear on all devices in real time.
     */
    fun observeMessages(ticketFirestoreId: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = messagesCol(ticketFirestoreId)
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val docs = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.also { it["id"] = doc.id }
                }
                trySend(docs)
            }
        awaitClose { registration?.remove() }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Pushes a new message to Firestore. Returns the generated document ID.
     * The real-time listener above will pick it up and emit it to all devices.
     */
    suspend fun sendMessage(
        ticketFirestoreId: String,
        senderUsername: String,
        senderDisplayName: String,
        message: String
    ): String {
        val sentAt = System.currentTimeMillis()
        val data = mapOf(
            "ticketFirestoreId"  to ticketFirestoreId,
            "senderUsername"     to senderUsername,
            "senderDisplayName"  to senderDisplayName,
            "message"            to message,
            "sentAt"             to sentAt
        )
        val ref = messagesCol(ticketFirestoreId).add(data).await()
        return ref.id
    }
}
