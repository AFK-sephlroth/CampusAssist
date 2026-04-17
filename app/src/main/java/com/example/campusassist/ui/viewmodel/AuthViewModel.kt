package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.SessionManager
import com.example.campusassist.domain.model.Department
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.domain.repository.DepartmentRepository
import com.example.campusassist.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Auth state (login / session) ─────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

// ── Registration state ────────────────────────────────────────────────────────

data class RegisterUiState(
    // Shared fields
    val role: UserRole = UserRole.USER,
    val username: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // Staff-only fields
    val selectedDepartment: Department? = null,

    // Async helpers
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val departmentRepository: DepartmentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState(isLoading = true))
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    init {
        checkSession()
        loadDepartments()
    }

    // ── Session ───────────────────────────────────────────────────────────────

    private fun checkSession() {
        viewModelScope.launch {
            val userId = sessionManager.userId.value
            if (userId != null) {
                val user = userRepository.getUserById(userId)
                _authState.value = AuthUiState(
                    isLoading  = false,
                    isLoggedIn = user != null,
                    currentUser = user
                )
            } else {
                _authState.value = AuthUiState(isLoading = false)
            }
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    fun login(id: String, password: String) {
        if (id.isBlank() || password.isBlank()) {
            _authState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = userRepository.login(id.trim(), password)
            if (user != null) {
                sessionManager.saveSession(user.id, user.role.name, user.name)
                _authState.value = AuthUiState(isLoggedIn = true, currentUser = user)
            } else {
                _authState.update { it.copy(isLoading = false, errorMessage = "Invalid ID or password") }
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthUiState()
    }

    // ── Registration — departments ────────────────────────────────────────────

    private fun loadDepartments() {
        viewModelScope.launch {
            departmentRepository.getAllDepartments()
                .catch { /* silently ignore; dropdown will just be empty */ }
                .collect { list ->
                    _registerState.update { it.copy(departments = list) }
                }
        }
    }

    // ── Registration — field updates ──────────────────────────────────────────

    fun onRoleChange(role: UserRole) {
        // Reset role-specific fields when switching to avoid stale state
        _registerState.update {
            it.copy(
                role = role,
                selectedDepartment = null,
                errorMessage = null
            )
        }
    }

    fun onUsernameChange(v: String)           = _registerState.update { it.copy(username = v) }
    fun onFullNameChange(v: String)           = _registerState.update { it.copy(fullName = v) }
    fun onPasswordChange(v: String)           = _registerState.update { it.copy(password = v) }
    fun onConfirmPasswordChange(v: String)    = _registerState.update { it.copy(confirmPassword = v) }
    fun onDepartmentChange(dept: Department?) = _registerState.update { it.copy(selectedDepartment = dept) }

    // ── Registration — submit ─────────────────────────────────────────────────

    fun register() {
        val s = _registerState.value

        // Validate shared fields
        when {
            s.username.isBlank() ->
                return _registerState.update { it.copy(errorMessage = "Username is required") }
            s.fullName.isBlank() ->
                return _registerState.update { it.copy(errorMessage = "Full name is required") }
            s.password.length < 6 ->
                return _registerState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            s.password != s.confirmPassword ->
                return _registerState.update { it.copy(errorMessage = "Passwords do not match") }
        }

        // Validate Staff-only fields
        if (s.role == UserRole.STAFF && s.selectedDepartment == null) {
            return _registerState.update { it.copy(errorMessage = "Please select a department") }
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = User(
                    id         = s.username.trim(),
                    name       = s.fullName.trim(),
                    department = if (s.role == UserRole.STAFF) s.selectedDepartment?.name else null,
                    role       = s.role
                )
                userRepository.register(user, s.password)
                _registerState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _registerState.update {
                    it.copy(isLoading = false, errorMessage = "Username already taken")
                }
            }
        }
    }
}