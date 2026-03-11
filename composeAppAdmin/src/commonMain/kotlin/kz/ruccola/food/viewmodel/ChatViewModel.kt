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

    fun setChatId(chatId: Int?) {
        if (_uiState.value.chatId == chatId) return

        pollingJob?.cancel()
        _uiState.update {
            it.copy(
                chatId = chatId,
                chat = null,
                messages = emptyList(),
                error = null,
                isLoading = false,
            )
        }

        if (chatId != null) {
            loadChat(chatId)
            startPolling(chatId)
        }
    }

    private fun loadChat(chatId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val chat = api.getChat(chatId)
                _uiState.update { it.copy(chat = chat) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startPolling(chatId: Int) {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(chatId)
                delay(5_000)
            }
        }
    }

    private suspend fun loadMessages(chatId: Int) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val messages = api.getMessages(chatId)
            val lastId = messages.lastOrNull()?.id
            val currentChat = _uiState.value.chat
            if (lastId != null && currentChat?.lastReadMessageId != lastId) {
                api.markRead(chatId, MarkReadDto(lastId))
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
        val chatId = state.chatId ?: return
        val body = state.messageBody.trim()

        if (body.isBlank() || body.length > MESSAGE_BODY_MAX_LENGTH) return

        _uiState.update { it.copy(messageBody = "") }
        viewModelScope.launch {
            try {
                api.sendMessage(chatId, MessageSendDto(body))
                loadMessages(chatId)
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
    val chatId: Int? = null,
    val chat: ChatDto? = null,
    val messages: List<MessageDto> = emptyList(),
    val messageBody: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
