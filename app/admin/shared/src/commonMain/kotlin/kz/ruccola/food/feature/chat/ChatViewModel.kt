package kz.ruccola.food.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
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

class ChatViewModel(
    private val api: ChatApi = ChatApi(),
) : ViewModel() {
    val uiState: StateFlow<ChatUiState>
        field = MutableStateFlow(ChatUiState())

    private var pollingJob: Job? = null

    fun setChatId(chatId: Int?) {
        if (uiState.value.chatId == chatId) return

        pollingJob?.cancel()
        uiState.update {
            it.copy(
                chatId = chatId,
                chat = null,
                messages = emptyList(),
                messageBody = "",
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
            uiState.update { it.copy(isLoading = true) }
            try {
                val chat = api.getChat(chatId)
                uiState.update { it.copy(chat = chat) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Error") }
            } finally {
                uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startPolling(chatId: Int) {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(chatId)
                delay(5.seconds)
            }
        }
    }

    private suspend fun loadMessages(chatId: Int) {
        try {
            val messages = api.getMessages(chatId)
            val lastId = messages.lastOrNull()?.id
            val currentChat = uiState.value.chat
            if (lastId != null && currentChat?.lastReadMessageId != lastId) {
                api.markRead(chatId, MarkReadDto(lastId))
                uiState.update { state ->
                    state.copy(chat = state.chat?.copy(lastReadMessageId = lastId))
                }
            }
            uiState.update { it.copy(messages = messages) }
        } catch (e: Exception) {
            uiState.update { it.copy(error = e.message ?: "Error") }
        }
    }

    fun onMessageBodyChange(body: String) {
        uiState.update { it.copy(messageBody = body) }
    }

    fun sendMessage() {
        val state = uiState.value
        val chatId = state.chatId ?: return
        val body = state.messageBody.trim()

        if (body.isBlank() || body.length > MESSAGE_BODY_MAX_LENGTH) return

        uiState.update { it.copy(messageBody = "") }
        viewModelScope.launch {
            try {
                api.sendMessage(chatId, MessageSendDto(body))
                loadMessages(chatId)
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Error") }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(api: ChatApi = ChatApi()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { ChatViewModel(api) }
            }
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
