package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.NetworkMonitor
import com.example.campusassist.data.remote.FirebaseTicketSource
import com.example.campusassist.data.repository.TicketRepositoryImpl
import com.example.campusassist.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val lastSyncMessage: String? = null,
    val showSyncBanner: Boolean = false
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val firestoreSource: FirebaseTicketSource,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        networkMonitor.startMonitoring()
        observeConnectivity()
        startFirestoreListener()
    }

    // ── Real-time Firestore → Room listener ───────────────────────────────────

    private fun startFirestoreListener() {
        viewModelScope.launch {
            firestoreSource.observeAllTickets()
                .catch { /* Firestore unavailable — Room data stays as-is */ }
                .collect {
                    // Each emission means Firestore changed — pull into Room
                    runCatching { ticketRepository.syncFromFirestore() }
                }
        }
    }

    // ── Connectivity watcher ──────────────────────────────────────────────────

    private fun observeConnectivity() {
        viewModelScope.launch {
            // NetworkMonitor exposes `isOnline`, not `isConnected`
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
                if (isOnline) {
                    // Back online — push any locally-created/edited tickets
                    runCatching { syncUnsynced() }
                }
            }
        }
    }

    // ── Manual sync ───────────────────────────────────────────────────────────

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, lastSyncMessage = null, showSyncBanner = false) }
            try {
                ticketRepository.syncFromFirestore()
                syncUnsynced()
                _uiState.update {
                    it.copy(isSyncing = false, lastSyncMessage = "Synced successfully", showSyncBanner = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSyncing = false, lastSyncMessage = "Sync failed: ${e.message}", showSyncBanner = true)
                }
            }
        }
    }

    fun dismissSyncBanner() {
        _uiState.update { it.copy(showSyncBanner = false, lastSyncMessage = null) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun syncUnsynced() {
        (ticketRepository as? TicketRepositoryImpl)?.syncUnsyncedTickets()
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}
