package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.SessionManager
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val studentId: String = "",
    val email: String = "",
    val department: String = "",
    val contactNumber: String = "",
    val role: UserRole = UserRole.STUDENT,
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState(isLoading = true))
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val userId = sessionManager.userId.value
            if (userId != null) {
                val user = userRepository.getUserById(userId)
                _authState.value = AuthUiState(
                    isLoading = false,
                    isLoggedIn = user != null,
                    currentUser = user
                )
            } else {
                _authState.value = AuthUiState(isLoading = false)
            }
        }
    }

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

    // Register form field updates
    fun onNameChange(v: String)            = _registerState.update { it.copy(name = v) }
    fun onStudentIdChange(v: String)       = _registerState.update { it.copy(studentId = v) }
    fun onEmailChange(v: String)           = _registerState.update { it.copy(email = v) }
    fun onDepartmentChange(v: String)      = _registerState.update { it.copy(department = v) }
    fun onContactChange(v: String)         = _registerState.update { it.copy(contactNumber = v) }
    fun onRoleChange(v: UserRole)          = _registerState.update { it.copy(role = v) }
    fun onPasswordChange(v: String)        = _registerState.update { it.copy(password = v) }
    fun onConfirmPasswordChange(v: String) = _registerState.update { it.copy(confirmPassword = v) }

    fun register() {
        val s = _registerState.value
        when {
            s.name.isBlank() || s.studentId.isBlank() || s.password.isBlank() ->
                _registerState.update { it.copy(errorMessage = "Please fill in all required fields") }
            s.password != s.confirmPassword ->
                _registerState.update { it.copy(errorMessage = "Passwords do not match") }
            s.password.length < 6 ->
                _registerState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            else -> viewModelScope.launch {
                _registerState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val user = User(
                        id = s.studentId.trim(),
                        name = s.name,
                        email = s.email,
                        department = s.department,
                        contactNumber = s.contactNumber,
                        role = s.role
                    )
                    userRepository.register(user, s.password)
                    _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                } catch (e: Exception) {
                    _registerState.update { it.copy(isLoading = false, errorMessage = "ID already exists") }
                }
            }
        }
    }
}