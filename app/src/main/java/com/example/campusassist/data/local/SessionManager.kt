package com.example.campusassist.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME  = "campus_session"
        const val KEY_USER_ID   = "user_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_USER_NAME = "user_name"
        const val KEY_THEME     = "theme"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Expose as StateFlow so ViewModels can collect reactively
    private val _userId   = MutableStateFlow<String?>(prefs.getString(KEY_USER_ID, null))
    private val _userRole = MutableStateFlow<String?>(prefs.getString(KEY_USER_ROLE, null))
    private val _userName = MutableStateFlow<String?>(prefs.getString(KEY_USER_NAME, null))
    private val _theme    = MutableStateFlow(prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM")

    val userId:   StateFlow<String?> = _userId.asStateFlow()
    val userRole: StateFlow<String?> = _userRole.asStateFlow()
    val userName: StateFlow<String?> = _userName.asStateFlow()
    val theme:    StateFlow<String>  = _theme.asStateFlow()

    fun saveSession(userId: String, role: String, name: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_NAME, name)
            .apply()
        _userId.value   = userId
        _userRole.value = role
        _userName.value = name
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _userId.value   = null
        _userRole.value = null
        _userName.value = null
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _theme.value = theme
    }

    fun isLoggedIn(): Boolean = prefs.getString(KEY_USER_ID, null) != null
}