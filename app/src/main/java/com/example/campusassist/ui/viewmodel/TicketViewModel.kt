package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.domain.model.ServiceCategory
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketPriority
import com.example.campusassist.domain.model.TicketStatus
import com.example.campusassist.domain.repository.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketListUiState(
    val tickets: List<ServiceTicket> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)

data class CreateTicketUiState(
    val title: String = "",
    val description: String = "",
    val category: ServiceCategory = ServiceCategory.IT,
    val priority: TicketPriority = TicketPriority.MEDIUM,
    val departmentId: Long? = null,
    val attachmentUris: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

private const val MAX_ATTACHMENTS = 3

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val repository: TicketRepository
) : ViewModel() {

    private val _listUiState = MutableStateFlow(TicketListUiState(isLoading = true))
    val listUiState: StateFlow<TicketListUiState> = _listUiState.asStateFlow()

    private val _createUiState = MutableStateFlow(CreateTicketUiState())
    val createUiState: StateFlow<CreateTicketUiState> = _createUiState.asStateFlow()

    private val _selectedTicket = MutableStateFlow<ServiceTicket?>(null)
    val selectedTicket: StateFlow<ServiceTicket?> = _selectedTicket.asStateFlow()

    init {
        loadTickets()
    }

    private fun loadTickets() {
        viewModelScope.launch {
            repository.getAllTickets()
                .catch { e ->
                    _listUiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { tickets ->
                    _listUiState.update {
                        it.copy(tickets = tickets, isLoading = false)
                    }
                }
        }
    }

    fun loadTicketById(id: Long) {
        viewModelScope.launch {
            _selectedTicket.value = repository.getTicketById(id)
        }
    }

    // ── Create ticket form updates ────────────────────────────────────────────

    fun onTitleChange(title: String)           = _createUiState.update { it.copy(title = title) }
    fun onDescriptionChange(desc: String)      = _createUiState.update { it.copy(description = desc) }
    fun onCategoryChange(cat: ServiceCategory) = _createUiState.update { it.copy(category = cat) }
    fun onPriorityChange(pri: TicketPriority)  = _createUiState.update { it.copy(priority = pri) }

    fun onDepartmentChange(departmentId: Long?) =
        _createUiState.update { it.copy(departmentId = departmentId) }

    /**
     * Appends new URIs to the attachment list, capping at MAX_ATTACHMENTS (3).
     * Duplicate URIs are ignored.
     */
    fun onAttachmentsChange(uris: List<String>) {
        _createUiState.update { state ->
            val combined = (state.attachmentUris + uris).distinct()
            state.copy(attachmentUris = combined.take(MAX_ATTACHMENTS))
        }
    }

    /**
     * Removes a single attachment URI by value.
     */
    fun onAttachmentRemove(uri: String) {
        _createUiState.update { state ->
            state.copy(attachmentUris = state.attachmentUris.filter { it != uri })
        }
    }

    fun submitTicket() {
        val state = _createUiState.value
        if (state.title.isBlank()) {
            _createUiState.update { it.copy(errorMessage = "Title cannot be empty") }
            return
        }
        viewModelScope.launch {
            _createUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val ticket = ServiceTicket(
                    title          = state.title,
                    description    = state.description,
                    category       = state.category,
                    priority       = state.priority,
                    departmentId   = state.departmentId,
                    attachmentUris = state.attachmentUris
                        .filter { it.isNotBlank() }
                        .joinToString(",")
                        .ifEmpty { null }
                )
                repository.createTicket(ticket)
                _createUiState.update { it.copy(isLoading = false, isSuccess = true) }
                resetCreateForm()
            } catch (e: Exception) {
                _createUiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // ── Ticket updates ────────────────────────────────────────────────────────

    fun updateTicketStatus(ticket: ServiceTicket, newStatus: TicketStatus) {
        viewModelScope.launch {
            repository.updateStatus(ticket.id, newStatus)
            if (_selectedTicket.value?.id == ticket.id) {
                _selectedTicket.value = repository.getTicketById(ticket.id)
            }
        }
    }

    fun updateNotes(ticketId: Long, notes: String?) {
        viewModelScope.launch {
            repository.updateNotes(ticketId, notes)
            if (_selectedTicket.value?.id == ticketId) {
                _selectedTicket.value = repository.getTicketById(ticketId)
            }
        }
    }

    fun deleteTicket(ticket: ServiceTicket) {
        viewModelScope.launch {
            repository.deleteTicket(ticket)
        }
    }

    fun resetCreateForm() {
        _createUiState.value = CreateTicketUiState()
    }

    fun setOfflineMode(offline: Boolean) {
        _listUiState.update { it.copy(isOffline = offline) }
    }
}
