package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.domain.model.AppNotification
import com.example.campusassist.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)

    val notifications: StateFlow<List<AppNotification>> = _userId
        .filterNotNull()
        .flatMapLatest { repository.getNotifications(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = _userId
        .filterNotNull()
        .flatMapLatest { repository.getUnreadCount(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setUser(userId: String) {
        _userId.value = userId
    }

    fun markAsRead(id: Long) = viewModelScope.launch {
        repository.markAsRead(id)
    }

    fun markAllAsRead() = viewModelScope.launch {
        _userId.value?.let { repository.markAllAsRead(it) }
    }

    fun clearAll() = viewModelScope.launch {
        _userId.value?.let { repository.clearAll(it) }
    }
}