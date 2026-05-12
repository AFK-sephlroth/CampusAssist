package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.dao.ChatMessageDao
import com.example.campusassist.data.local.entity.ChatMessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long,
    val ticketId: Long,
    val senderUsername: String,
    val senderDisplayName: String,
    val message: String,
    val sentAt: Long
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) : ViewModel() {

    private val _ticketId = MutableStateFlow<Long?>(null)

    val messages: StateFlow<List<ChatMessage>> = _ticketId
        .filterNotNull()
        .flatMapLatest { id ->
            chatMessageDao.getMessagesForTicket(id)
                .map { list ->
                    list.map { e ->
                        ChatMessage(
                            id = e.id,
                            ticketId = e.ticketId,
                            senderUsername = e.senderUsername,
                            senderDisplayName = e.senderDisplayName,
                            message = e.message,
                            sentAt = e.sentAt
                        )
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTicket(ticketId: Long) {
        _ticketId.value = ticketId
    }

    fun sendMessage(ticketId: Long, senderUsername: String, senderDisplayName: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    ticketId = ticketId,
                    senderUsername = senderUsername,
                    senderDisplayName = senderDisplayName,
                    message = message.trim()
                )
            )
        }
    }
}
