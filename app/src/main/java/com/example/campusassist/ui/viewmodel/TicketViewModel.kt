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
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

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

    // Create ticket form updates
    fun onTitleChange(title: String) = _createUiState.update { it.copy(title = title) }
    fun onDescriptionChange(desc: String) = _createUiState.update { it.copy(description = desc) }
    fun onCategoryChange(cat: ServiceCategory) = _createUiState.update { it.copy(category = cat) }
    fun onPriorityChange(pri: TicketPriority) = _createUiState.update { it.copy(priority = pri) }

    fun onDepartmentChange(departmentId: Long?) =
        _createUiState.update { it.copy(departmentId = departmentId) }

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
                    title = state.title,
                    description = state.description,
                    category = state.category,
                    priority = state.priority,
                    departmentId = state.departmentId
                )
                repository.createTicket(ticket)
                _createUiState.update { it.copy(isLoading = false, isSuccess = true) }
                resetCreateForm()
            } catch (e: Exception) {
                _createUiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateTicketStatus(ticket: ServiceTicket, newStatus: TicketStatus) {
        viewModelScope.launch {
            repository.updateTicket(ticket.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
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