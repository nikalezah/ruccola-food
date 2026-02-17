package kz.ruccola.food.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.ruccola.food.R
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatDto
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.api.MessageSendDto
import kz.ruccola.food.ui.ChatUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidChatScreen(
    token: String,
    onBack: (() -> Unit)? = null,
) {
    val chatTitle = stringResource(R.string.chat_support_title)
    val errorFallback = stringResource(R.string.error_generic)
    val scope = rememberCoroutineScope()
    val api = remember { ChatApi() }
    val currentUserId = remember(token) { parseUserId(token) }
    var chat by remember { mutableStateOf<ChatDto?>(null) }
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var messageBody by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val errorText = error?.let { stringResource(R.string.error_prefix, it) }

    fun loadChat() {
        scope.launch {
            isLoading = true
            try {
                chat = api.getMyChat(token)
            } catch (e: Exception) {
                error = e.message ?: errorFallback
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun loadMessages(chatId: Int) {
        isLoading = true
        try {
            messages = api.getMessages(token, chatId)
            val lastId = messages.lastOrNull()?.id
            if (lastId != null && chat?.lastReadMessageId != lastId) {
                api.markRead(token, chatId, MarkReadDto(lastId))
                chat = chat?.copy(lastReadMessageId = lastId)
            }
        } catch (e: Exception) {
            error = e.message ?: errorFallback
        } finally {
            isLoading = false
        }
    }

    fun sendMessage() {
        val chatId = chat?.id ?: return
        val body = messageBody.trim()
        if (body.isBlank()) return
        messageBody = ""
        scope.launch {
            try {
                api.sendMessage(token, chatId, MessageSendDto(body))
                loadMessages(chatId)
            } catch (e: Exception) {
                error = e.message ?: errorFallback
            }
        }
    }

    LaunchedEffect(Unit) {
        loadChat()
    }

    LaunchedEffect(chat?.id) {
        val chatId = chat?.id ?: return@LaunchedEffect
        while (isActive) {
            loadMessages(chatId)
            delay(5_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatTitle) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
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
            placeholder = stringResource(R.string.chat_placeholder),
            emptyText = stringResource(R.string.chat_empty),
            errorText = errorText,
            isLoading = isLoading,
            inputEnabled = true,
            sendEnabled = messageBody.isNotBlank(),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
        )
    }
}
