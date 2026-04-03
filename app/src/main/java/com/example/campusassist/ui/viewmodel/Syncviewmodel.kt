package com.example.campusassist.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.campusassist.data.local.NetworkMonitor
import com.example.campusassist.worker.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String? = null,
    val showSyncBanner: Boolean = false   // shown when sync just completed
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _syncState = MutableStateFlow(SyncUiState())
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private var currentUserId: String? = null

    init {
        networkMonitor.startMonitoring()
        observeNetworkChanges()
        observeWorkState()
    }

    fun setUser(userId: String) {
        currentUserId = userId
        // Schedule periodic background sync
        SyncWorker.schedulePeriodic(context, userId)
    }

    /** Call this when the user manually pulls to refresh or taps sync */
    fun syncNow() {
        val userId = currentUserId ?: return
        if (!networkMonitor.isOnline.value) {
            _syncState.update { it.copy(lastSyncMessage = "No internet connection") }
            return
        }
        SyncWorker.scheduleOneTime(context, userId)
    }

    fun dismissSyncBanner() {
        _syncState.update { it.copy(showSyncBanner = false, lastSyncMessage = null) }
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .collect { online ->
                    _syncState.update { it.copy(isOnline = online) }

                    // Auto-trigger sync when coming back online
                    if (online) {
                        currentUserId?.let { uid ->
                            SyncWorker.scheduleOneTime(context, uid)
                        }
                    }
                }
        }
    }

    private fun observeWorkState() {
        viewModelScope.launch {
            workManager
                .getWorkInfosByTagFlow(SyncWorker.WORK_NAME_ONESHOT)
                .collect { workInfoList ->
                    val info = workInfoList.firstOrNull() ?: return@collect

                    when (info.state) {
                        WorkInfo.State.RUNNING -> {
                            _syncState.update { it.copy(isSyncing = true) }
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            val synced   = info.outputData.getInt("synced_count", 0)
                            val conflicts = info.outputData.getInt("conflict_count", 0)
                            val msg = when {
                                synced == 0   -> "Already up to date"
                                conflicts > 0 -> "Synced $synced ticket(s), $conflicts conflict(s)"
                                else          -> "Synced $synced ticket(s) successfully"
                            }
                            _syncState.update {
                                it.copy(
                                    isSyncing = false,
                                    lastSyncMessage = msg,
                                    showSyncBanner = synced > 0
                                )
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            _syncState.update {
                                it.copy(isSyncing = false, lastSyncMessage = "Sync failed. Will retry.")
                            }
                        }
                        WorkInfo.State.CANCELLED -> {
                            _syncState.update { it.copy(isSyncing = false) }
                        }
                        else -> { /* ENQUEUED / BLOCKED — no UI change needed */ }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}