package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.ruccola.food.MESSAGE_BODY_MAX_LENGTH
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatDto
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.api.MessageSendDto

class ChatViewModel : ViewModel() {
    private val api = ChatApi()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadChat(
        token: String,
        isRefreshing: Boolean = false,
    ) {
        viewModelScope.launch {
            if (isRefreshing) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val chat = api.getMyChat(token)
                _uiState.update { it.copy(chat = chat, error = null) }
                loadMessages(token, chat.id)
                loadLastMessage(token, chat)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    private suspend fun loadLastMessage(
        token: String,
        chat: ChatDto,
    ) {
        try {
            val afterId = chat.lastMessageId?.minus(1)
            val lastMessage = api.getMessages(
                token = token,
                chatId = chat.id,
                afterId = afterId,
                limit = 1,
            ).lastOrNull()
            _uiState.update { it.copy(lastMessage = lastMessage) }
        } catch (e: Exception) {
            // Non-critical error
        }
    }

    private suspend fun loadMessages(
        token: String,
        chatId: Int,
    ) {
        try {
            val messages = api.getMessages(token, chatId)
            _uiState.update { it.copy(messages = messages) }

            val lastId = messages.lastOrNull()?.id
            val currentChat = _uiState.value.chat
            if (lastId != null && currentChat != null && currentChat.lastReadMessageId != lastId) {
                api.markRead(token, chatId, MarkReadDto(lastId))
                _uiState.update { it.copy(chat = currentChat.copy(lastReadMessageId = lastId)) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: e.toString()) }
        }
    }

    fun startPolling(token: String) {
        viewModelScope.launch {
            while (isActive) {
                val chatId = _uiState.value.chat?.id
                if (chatId != null) {
                    loadMessages(token, chatId)
                }
                delay(5_000)
            }
        }
    }

    fun sendMessage(
        token: String,
        body: String,
    ) {
        val chatId = _uiState.value.chat?.id ?: return
        val trimmedBody = body.trim()
        if (trimmedBody.isBlank() || trimmedBody.length > MESSAGE_BODY_MAX_LENGTH) return

        viewModelScope.launch {
            try {
                api.sendMessage(token, chatId, MessageSendDto(trimmedBody))
                loadMessages(token, chatId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: e.toString()) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
