package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.SessionManager
import com.example.campusassist.data.remote.FirebaseAuthSource
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.domain.repository.DepartmentRepository
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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val departmentRepository: DepartmentRepository,
    private val sessionManager: SessionManager,
    private val firebaseAuth: FirebaseAuthSource       // direct access for session check
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState(isLoading = true))
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    init {
        checkSession()
    }

    // ── Session check ─────────────────────────────────────────────────────────
    /**
     * On app start: if Firebase still has a signed-in user (token not revoked),
     * we honour that session. Otherwise we fall through to the local Room check
     * so offline-first login still works.
     */
    private fun checkSession() {
        viewModelScope.launch {
            // Firebase already has a persisted session — no round-trip needed
            if (firebaseAuth.isSignedIn) {
                val profile = firebaseAuth.fetchCurrentUserProfile()
                if (profile != null) {
                    val username = profile["username"] as? String ?: ""
                    val fullname = profile["fullname"] as? String ?: ""
                    val role     = profile["role"] as? String ?: "USER"
                    sessionManager.saveSession(username, role, fullname)
                    // Still load from Room so profile image URI is included
                    val user = userRepository.getUserById(username)
                        ?: buildUserFromProfile(profile)
                    // Sync departments so the New Request dropdown is always up to date
                    runCatching { departmentRepository.syncFromFirestore() }
                    _authState.value = AuthUiState(isLoading = false, isLoggedIn = true, currentUser = user)
                    return@launch
                }
            }

            // Fall back to local session (offline / no Firebase user)
            val userId = sessionManager.userId.value
            if (userId != null) {
                val user = userRepository.getUserById(userId)
                // Sync departments so the New Request dropdown is always up to date
                if (user != null) runCatching { departmentRepository.syncFromFirestore() }
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
                sessionManager.saveSession(user.username, user.role.name, user.fullname)
                // Sync departments so the New Request dropdown is always up to date
                runCatching { departmentRepository.syncFromFirestore() }
                _authState.value = AuthUiState(isLoggedIn = true, currentUser = user)
            } else {
                _authState.update { it.copy(isLoading = false, errorMessage = "Invalid username or password") }
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        firebaseAuth.signOut()          // clears Firebase token
        sessionManager.clearSession()   // clears local prefs
        _authState.value = AuthUiState()
    }

    // ── Profile image ─────────────────────────────────────────────────────────

    fun updateProfileImage(uri: String?) {
        val username = _authState.value.currentUser?.username ?: return
        viewModelScope.launch {
            userRepository.updateProfileImage(username, uri)
            val updated = userRepository.getUserById(username)
            _authState.update { it.copy(currentUser = updated) }
        }
    }

    // ── Registration — field updates ──────────────────────────────────────────

    fun onRoleChange(role: UserRole)           = _registerState.update { it.copy(role = role, departmentText = "", errorMessage = null) }
    fun onUsernameChange(v: String)            = _registerState.update { it.copy(username = v) }
    fun onFullNameChange(v: String)            = _registerState.update { it.copy(fullName = v) }
    fun onPasswordChange(v: String)            = _registerState.update { it.copy(password = v) }
    fun onConfirmPasswordChange(v: String)     = _registerState.update { it.copy(confirmPassword = v) }
    fun onDepartmentTextChange(v: String)      = _registerState.update { it.copy(departmentText = v) }
    fun resetRegisterState()                   { _registerState.value = RegisterUiState() }

    // ── Registration — submit ─────────────────────────────────────────────────

    fun register() {
        val s = _registerState.value
        when {
            s.username.isBlank() -> return _registerState.update { it.copy(errorMessage = "Username is required") }
            s.fullName.isBlank() -> return _registerState.update { it.copy(errorMessage = "Full name is required") }
            s.password.length < 6 -> return _registerState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            s.password != s.confirmPassword -> return _registerState.update { it.copy(errorMessage = "Passwords do not match") }
        }
        if (s.role == UserRole.STAFF && s.departmentText.isBlank()) {
            return _registerState.update { it.copy(errorMessage = "Department is required for staff") }
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val departmentName = if (s.role == UserRole.STAFF) {
                    departmentRepository.getOrCreateByName(s.departmentText).name
                } else null

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
                    it.copy(isLoading = false, errorMessage = e.message ?: "Registration failed")
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildUserFromProfile(profile: Map<String, Any?>): User {
        val roleStr = profile["role"] as? String ?: "USER"
        return User(
            username   = profile["username"] as? String ?: "",
            fullname   = profile["fullname"] as? String ?: "",
            role       = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.USER),
            department = profile["department"] as? String,
            createdAt  = (profile["createdAt"] as? Long) ?: System.currentTimeMillis(),
            isActive   = profile["isActive"] as? Boolean ?: true
        )
    }
}