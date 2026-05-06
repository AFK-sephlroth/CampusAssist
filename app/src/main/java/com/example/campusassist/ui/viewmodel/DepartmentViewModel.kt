package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.domain.model.Department
import com.example.campusassist.domain.repository.DepartmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DepartmentUiState(
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DepartmentViewModel @Inject constructor(
    private val repository: DepartmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepartmentUiState(isLoading = true))
    val uiState: StateFlow<DepartmentUiState> = _uiState.asStateFlow()

    init {
        loadDepartments()
    }

    private fun loadDepartments() {
        viewModelScope.launch {
            repository.getAllDepartments()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { departments ->
                    _uiState.update { it.copy(departments = departments, isLoading = false) }
                }
        }
    }

    fun addDepartment(name: String, code: String) {
        if (name.isBlank() || code.isBlank()) return
        viewModelScope.launch {
            repository.addDepartment(Department(name = name, code = code.uppercase()))
        }
    }

    fun updateDepartment(department: Department) {
        viewModelScope.launch {
            repository.updateDepartment(department)
        }
    }

    fun deleteDepartment(department: Department) {
        viewModelScope.launch {
            repository.deleteDepartment(department)
        }
    }
}