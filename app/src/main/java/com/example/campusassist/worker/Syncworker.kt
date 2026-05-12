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

            // Delegate to the repository which handles Firestore push + markAsSynced
            (ticketRepository as? com.example.campusassist.data.repository.TicketRepositoryImpl)
                ?.syncUnsyncedTickets()

            val syncedCount = unsyncedTickets.size

            if (syncedCount > 0) {
                notificationRepository.addNotification(
                    AppNotification(
                        userId   = userId,
                        ticketId = 0,
                        title    = "Sync Complete",
                        message  = "$syncedCount ticket${if (syncedCount > 1) "s" else ""} synced successfully.",
                        type     = NotificationType.SYNC_COMPLETE
                    )
                )
            }

            Log.d(TAG, "Sync done. Synced: $syncedCount")
            Result.success(workDataOf("synced_count" to syncedCount))

        } catch (e: Exception) {
            Log.e(TAG, "Sync failed entirely", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

