package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.ruccola.food.MESSAGE_BODY_MAX_LENGTH
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatDto
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.api.MessageSendDto
import kotlin.time.Duration.Companion.seconds

class ChatViewModel : ViewModel() {
    private val api = ChatApi()

    val uiState: StateFlow<ChatUiState>
        field = MutableStateFlow(ChatUiState())

    fun loadChat(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) {
                uiState.update { it.copy(isRefreshing = true) }
            } else {
                uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val chat = api.getMyChat()
                uiState.update { it.copy(chat = chat, error = null) }
                loadMessages(chat.id)
                loadLastMessage(chat)
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    private suspend fun loadLastMessage(chat: ChatDto) {
        try {
            val afterId = chat.lastMessageId?.minus(1)
            val lastMessage = api.getMessages(
                chatId = chat.id,
                afterId = afterId,
                limit = 1,
            ).lastOrNull()
            uiState.update { it.copy(lastMessage = lastMessage) }
        } catch (e: Exception) {
            // Non-critical error
        }
    }

    private suspend fun loadMessages(chatId: Int) {
        try {
            val messages = api.getMessages(chatId)
            uiState.update { it.copy(messages = messages) }

            val lastId = messages.lastOrNull()?.id
            val currentChat = uiState.value.chat
            if (lastId != null && currentChat != null && currentChat.lastReadMessageId != lastId) {
                api.markRead(chatId, MarkReadDto(lastId))
                uiState.update { it.copy(chat = currentChat.copy(lastReadMessageId = lastId)) }
            }
        } catch (e: Exception) {
            uiState.update { it.copy(error = e.message ?: e.toString()) }
        }
    }

    fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                val chatId = uiState.value.chat?.id
                if (chatId != null) {
                    loadMessages(chatId)
                }
                delay(5.seconds)
            }
        }
    }

    fun sendMessage(body: String) {
        val chatId = uiState.value.chat?.id ?: return
        val trimmedBody = body.trim()
        if (trimmedBody.isBlank() || trimmedBody.length > MESSAGE_BODY_MAX_LENGTH) return

        viewModelScope.launch {
            try {
                api.sendMessage(chatId, MessageSendDto(trimmedBody))
                loadMessages(chatId)
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: e.toString()) }
            }
        }
    }

    fun clearError() {
        uiState.update { it.copy(error = null) }
    }
}

data class ChatUiState(
    val chat: ChatDto? = null,
    val messages: List<MessageDto> = emptyList(),
    val lastMessage: MessageDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
