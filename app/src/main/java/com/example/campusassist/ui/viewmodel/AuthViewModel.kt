package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.SessionManager
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
    val role: UserRole = UserRole.USER,
    val username: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val departmentText: String = "",
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
    }

    // ── Session ───────────────────────────────────────────────────────────────

    private fun checkSession() {
        viewModelScope.launch {
            val userId = sessionManager.userId.value
            if (userId != null) {
                val user = userRepository.getUserById(userId)
                _authState.value = AuthUiState(
                    isLoading   = false,
                    isLoggedIn  = user != null,
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
                sessionManager.saveSession(user.username, user.role.name, user.fullname)
                _authState.value = AuthUiState(isLoggedIn = true, currentUser = user)
            } else {
                _authState.update { it.copy(isLoading = false, errorMessage = "Invalid username or password") }
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthUiState()
    }

    fun updateProfileImage(uri: String?) {
        val username = _authState.value.currentUser?.username ?: return
        viewModelScope.launch {
            userRepository.updateProfileImage(username, uri)
            // Refresh the current user so the new URI propagates to the dashboard
            val updated = userRepository.getUserById(username)
            _authState.update { it.copy(currentUser = updated) }
        }
    }

    // ── Registration — field updates ──────────────────────────────────────────

    fun onRoleChange(role: UserRole) {
        _registerState.update {
            it.copy(role = role, departmentText = "", errorMessage = null)
        }
    }

    fun onUsernameChange(v: String)        = _registerState.update { it.copy(username = v) }
    fun onFullNameChange(v: String)        = _registerState.update { it.copy(fullName = v) }
    fun onPasswordChange(v: String)        = _registerState.update { it.copy(password = v) }
    fun onConfirmPasswordChange(v: String) = _registerState.update { it.copy(confirmPassword = v) }
    fun onDepartmentTextChange(v: String)  = _registerState.update { it.copy(departmentText = v) }

    // Call this when entering the Register screen to clear any stale success
    // state from a previous registration in the same session.  Without this,
    // LaunchedEffect(state.isSuccess) fires immediately (isSuccess is still
    // true) and pops the user back to Login before they can fill in the form.
    fun resetRegisterState() {
        _registerState.value = RegisterUiState()
    }

    // ── Registration — submit ─────────────────────────────────────────────────

    fun register() {
        val s = _registerState.value

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

        if (s.role == UserRole.STAFF && s.departmentText.isBlank()) {
            return _registerState.update { it.copy(errorMessage = "Department is required") }
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val departmentName = if (s.role == UserRole.STAFF) {
                    departmentRepository.getOrCreateByName(s.departmentText).name
                } else {
                    null
                }
                val user = User(
                    username   = s.username.trim(),
                    fullname   = s.fullName.trim(),
                    department = departmentName,
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
