package com.example.campusassist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusassist.data.local.dao.ChatMessageDao
import com.example.campusassist.data.local.dao.ServiceTicketDao
import com.example.campusassist.data.local.entity.ChatMessageEntity
import com.example.campusassist.data.remote.FirebaseChatSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long,
    val firestoreId: String?,
    val ticketId: Long,
    val senderUsername: String,
    val senderDisplayName: String,
    val message: String,
    val sentAt: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val serviceTicketDao: ServiceTicketDao,
    private val firebaseChatSource: FirebaseChatSource
) : ViewModel() {

    private val _ticketId = MutableStateFlow<Long?>(null)

    private val _firestoreTicketId: StateFlow<String?> = _ticketId
        .filterNotNull()
        .flatMapLatest { localId ->
            serviceTicketDao.observeTicketById(localId)
                .map { it?.firestoreId }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: StateFlow<List<ChatMessage>> = combine(
        _ticketId.filterNotNull(),
        _firestoreTicketId
    ) { localId, firestoreId -> localId to firestoreId }
        .flatMapLatest { (localId, firestoreId) ->
            if (firestoreId != null) {
                firebaseChatSource.observeMessages(firestoreId)
                    .onEach { remoteMsgs -> cacheRemoteMessages(localId, remoteMsgs) }
                    .map { remoteMsgs -> remoteMsgs.toUiMessages(localId) }
            } else {
                chatMessageDao.getMessagesForTicket(localId)
                    .map { entities ->
                        entities.map { e ->
                            ChatMessage(
                                id = e.id, firestoreId = null,
                                ticketId = e.ticketId,
                                senderUsername = e.senderUsername,
                                senderDisplayName = e.senderDisplayName,
                                message = e.message, sentAt = e.sentAt
                            )
                        }
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTicket(ticketId: Long) {
        _ticketId.value = ticketId
    }

    fun sendMessage(
        ticketId: Long,
        senderUsername: String,
        senderDisplayName: String,
        message: String
    ) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val firestoreId = _firestoreTicketId.value
            if (firestoreId != null) {
                try {
                    firebaseChatSource.sendMessage(
                        ticketFirestoreId = firestoreId,
                        senderUsername    = senderUsername,
                        senderDisplayName = senderDisplayName,
                        message           = message.trim()
                    )
                } catch (_: Exception) {
                    // Offline fallback
                    chatMessageDao.insertMessage(
                        ChatMessageEntity(
                            ticketId          = ticketId,
                            senderUsername    = senderUsername,
                            senderDisplayName = senderDisplayName,
                            message           = message.trim()
                        )
                    )
                }
            } else {
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        ticketId          = ticketId,
                        senderUsername    = senderUsername,
                        senderDisplayName = senderDisplayName,
                        message           = message.trim()
                    )
                )
            }
        }
    }

    private suspend fun cacheRemoteMessages(
        localTicketId: Long,
        remoteMsgs: List<Map<String, Any?>>
    ) {
        for (msg in remoteMsgs) {
            val fsId = msg["id"] as? String ?: continue
            if (chatMessageDao.getByFirestoreId(fsId) == null) {
                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        ticketId          = localTicketId,
                        senderUsername    = msg["senderUsername"]    as? String ?: "",
                        senderDisplayName = msg["senderDisplayName"] as? String ?: "",
                        message           = msg["message"]           as? String ?: "",
                        sentAt            = (msg["sentAt"]           as? Long)  ?: System.currentTimeMillis(),
                        firestoreId       = fsId
                    )
                )
            }
        }
    }

    private fun List<Map<String, Any?>>.toUiMessages(localTicketId: Long): List<ChatMessage> =
        mapIndexed { index, msg ->
            ChatMessage(
                id                = (index + 1).toLong(),
                firestoreId       = msg["id"] as? String,
                ticketId          = localTicketId,
                senderUsername    = msg["senderUsername"]    as? String ?: "",
                senderDisplayName = msg["senderDisplayName"] as? String ?: "",
                message           = msg["message"]           as? String ?: "",
                sentAt            = (msg["sentAt"]           as? Long)  ?: 0L
            )
        }
}
