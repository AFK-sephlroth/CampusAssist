package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.campusassist.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val theme: StateFlow<String> = sessionManager.theme

    fun setTheme(theme: String) {
        sessionManager.setTheme(theme)
    }
}