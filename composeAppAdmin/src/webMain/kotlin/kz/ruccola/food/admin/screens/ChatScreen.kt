package kz.ruccola.food.admin.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.ruccola.food.MESSAGE_BODY_MAX_LENGTH
import kz.ruccola.food.Strings
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatDto
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.api.MessageSendDto
import kz.ruccola.food.ui.ChatUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    token: String,
    chatId: Int?,
    customerName: String,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val api = remember { ChatApi() }
    val currentUserId = remember(token) { parseUserId(token) }
    var chat by remember { mutableStateOf<ChatDto?>(null) }
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var messageBody by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val errorText = error?.let { Strings.errorPrefix.replace("%s", it) }

    fun loadChat() {
        val safeChatId = chatId ?: return
        scope.launch {
            isLoading = true
            try {
                chat = api.getChat(token, safeChatId)
            } catch (e: Exception) {
                error = e.message ?: Strings.error
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun loadMessages(safeChatId: Int) {
        isLoading = true
        try {
            messages = api.getMessages(token, safeChatId)
            val lastId = messages.lastOrNull()?.id
            if (lastId != null && chat?.lastReadMessageId != lastId) {
                api.markRead(token, safeChatId, MarkReadDto(lastId))
                chat = chat?.copy(lastReadMessageId = lastId)
            }
        } catch (e: Exception) {
            error = e.message ?: Strings.error
        } finally {
            isLoading = false
        }
    }

    fun sendMessage() {
        val safeChatId = chatId ?: return
        val body = messageBody.trim()
        if (body.isBlank()) return
        if (body.length > MESSAGE_BODY_MAX_LENGTH) return
        messageBody = ""
        scope.launch {
            try {
                api.sendMessage(token, safeChatId, MessageSendDto(body))
                loadMessages(safeChatId)
            } catch (e: Exception) {
                error = e.message ?: Strings.error
            }
        }
    }

    LaunchedEffect(chatId) {
        chat = null
        messages = emptyList()
        error = null
        if (chatId == null) {
            return@LaunchedEffect
        }
        loadChat()
        while (isActive) {
            loadMessages(chatId)
            delay(5_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customerName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Strings.backToLogin,
                        )
                    }
                },
            )
        },
    ) { padding ->
        ChatUi(
            messages = messages,
            currentUserId = currentUserId,
            messageBody = messageBody,
            onMessageBodyChange = { messageBody = it },
            onSendMessage = { sendMessage() },
            placeholder = Strings.chatMessagePlaceholder,
            emptyText = Strings.chatEmpty,
            errorText = errorText,
            isLoading = isLoading,
            inputEnabled = chatId != null,
            sendEnabled = chatId != null,
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
        )
    }
}

private fun parseUserId(token: String): Int? {
    if (!token.startsWith("dummy-token-")) return null
    return token.split("-").lastOrNull()?.toIntOrNull()
}
