package com.example.campusassist.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.campusassist.domain.model.AppNotification
import com.example.campusassist.domain.model.NotificationType
import com.example.campusassist.domain.repository.NotificationRepository
import com.example.campusassist.domain.repository.TicketRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val ticketRepository: TicketRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME_PERIODIC = "campus_sync_periodic"
        const val WORK_NAME_ONESHOT  = "campus_sync_oneshot"
        const val KEY_USER_ID        = "user_id"
        private const val TAG        = "SyncWorker"

        /**
         * Schedule a one-time sync immediately (call when app comes back online)
         */
        fun scheduleOneTime(context: Context, userId: String) {
            val data = workDataOf(KEY_USER_ID to userId)

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .addTag(WORK_NAME_ONESHOT)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME_ONESHOT,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        /**
         * Schedule periodic sync every 15 minutes while online
         */
        fun schedulePeriodic(context: Context, userId: String) {
            val data = workDataOf(KEY_USER_ID to userId)

            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORK_NAME_PERIODIC)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME_PERIODIC,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        /**
         * Cancel all sync work (call on logout)
         */
        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(WORK_NAME_ONESHOT)
                cancelUniqueWork(WORK_NAME_PERIODIC)
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = inputData.getString(KEY_USER_ID) ?: return@withContext Result.failure()
        Log.d(TAG, "Starting sync for user: $userId")

        return@withContext try {
            val unsyncedTickets = ticketRepository.getUnsyncedTickets()
            Log.d(TAG, "Found ${unsyncedTickets.size} unsynced tickets")

            if (unsyncedTickets.isEmpty()) {
                Log.d(TAG, "Nothing to sync")
                return@withContext Result.success()
            }

            var syncedCount  = 0
            var conflictCount = 0

            for (ticket in unsyncedTickets) {
                try {
                    // ── In a real app, you would POST/PUT to your REST API or
                    //    Firestore here. For now we simulate with a delay and
                    //    mark the ticket as synced in Room.
                    //
                    // Example with Retrofit:
                    //   val response = apiService.uploadTicket(ticket.toApiModel())
                    //   if (response.isSuccessful) ticketRepository.markAsSynced(ticket.id)
                    //
                    // Example with Firestore:
                    //   firestore.collection("tickets").document(ticket.id.toString())
                    //       .set(ticket.toFirestoreMap()).await()
                    //   ticketRepository.markAsSynced(ticket.id)

                    // Simulate network call
                    kotlinx.coroutines.delay(100)

                    ticketRepository.markAsSynced(ticket.id)
                    syncedCount++

                    Log.d(TAG, "Synced ticket #${ticket.id}: ${ticket.title}")

                } catch (e: ConflictException) {
                    // Conflict: ticket was modified both locally and remotely
                    conflictCount++
                    Log.w(TAG, "Conflict on ticket #${ticket.id}: ${e.message}")

                    notificationRepository.addNotification(
                        AppNotification(
                            userId = userId,
                            ticketId = ticket.id,
                            title = "Sync Conflict",
                            message = "Ticket \"${ticket.title}\" has a conflict. Please review.",
                            type = NotificationType.CONFLICT
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync ticket #${ticket.id}", e)
                    // Don't fail the entire job — retry unsynced ones next time
                }
            }

            // Send a summary notification after sync completes
            if (syncedCount > 0) {
                notificationRepository.addNotification(
                    AppNotification(
                        userId = userId,
                        ticketId = 0,
                        title = "Sync Complete",
                        message = "$syncedCount ticket${if (syncedCount > 1) "s" else ""} synced successfully." +
                                if (conflictCount > 0) " $conflictCount conflict(s) need attention." else "",
                        type = NotificationType.SYNC_COMPLETE
                    )
                )
            }

            Log.d(TAG, "Sync done. Synced: $syncedCount, Conflicts: $conflictCount")
            Result.success(
                workDataOf(
                    "synced_count"   to syncedCount,
                    "conflict_count" to conflictCount
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Sync failed entirely", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

/** Thrown when a server-side conflict is detected during sync */
class ConflictException(message: String) : Exception(message)