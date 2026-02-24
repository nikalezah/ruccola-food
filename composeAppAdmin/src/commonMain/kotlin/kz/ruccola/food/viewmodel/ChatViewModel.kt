package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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

    private var pollingJob: Job? = null

    fun setChatId(
        token: String,
        chatId: Int?,
    ) {
        if (_uiState.value.chatId == chatId && _uiState.value.token == token) return

        pollingJob?.cancel()
        _uiState.update {
            it.copy(
                token = token,
                chatId = chatId,
                chat = null,
                messages = emptyList(),
                error = null,
                isLoading = false,
            )
        }

        if (chatId != null) {
            loadChat(token, chatId)
            startPolling(token, chatId)
        }
    }

    private fun loadChat(
        token: String,
        chatId: Int,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val chat = api.getChat(token, chatId)
                _uiState.update { it.copy(chat = chat) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startPolling(
        token: String,
        chatId: Int,
    ) {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(token, chatId)
                delay(5_000)
            }
        }
    }

    private suspend fun loadMessages(
        token: String,
        chatId: Int,
    ) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val messages = api.getMessages(token, chatId)
            val lastId = messages.lastOrNull()?.id
            val currentChat = _uiState.value.chat
            if (lastId != null && currentChat?.lastReadMessageId != lastId) {
                api.markRead(token, chatId, MarkReadDto(lastId))
                _uiState.update { it.copy(chat = currentChat?.copy(lastReadMessageId = lastId)) }
            }
            _uiState.update { it.copy(messages = messages) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "Error") }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onMessageBodyChange(body: String) {
        _uiState.update { it.copy(messageBody = body) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val token = state.token ?: return
        val chatId = state.chatId ?: return
        val body = state.messageBody.trim()

        if (body.isBlank() || body.length > MESSAGE_BODY_MAX_LENGTH) return

        _uiState.update { it.copy(messageBody = "") }
        viewModelScope.launch {
            try {
                api.sendMessage(token, chatId, MessageSendDto(body))
                loadMessages(token, chatId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error") }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}

data class ChatUiState(
    val token: String? = null,
    val chatId: Int? = null,
    val chat: ChatDto? = null,
    val messages: List<MessageDto> = emptyList(),
    val messageBody: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
