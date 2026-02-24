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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.ruccola.food.Strings
import kz.ruccola.food.ui.ChatUi
import kz.ruccola.food.viewmodel.ChatViewModel

fun parseUserId(token: String): Int? {
    if (!token.startsWith("dummy-token-")) return null
    return token.split("-").lastOrNull()?.toIntOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    token: String,
    chatId: Int?,
    customerName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel { ChatViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = remember(token) { parseUserId(token) }
    val errorText = uiState.error?.let { Strings.errorPrefix.replace("%s", it) }

    LaunchedEffect(token, chatId) {
        viewModel.setChatId(token, chatId)
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
            messages = uiState.messages,
            currentUserId = currentUserId,
            messageBody = uiState.messageBody,
            onMessageBodyChange = { viewModel.onMessageBodyChange(it) },
            onSendMessage = { viewModel.sendMessage() },
            placeholder = Strings.chatMessagePlaceholder,
            emptyText = Strings.chatEmpty,
            errorText = errorText,
            isLoading = uiState.isLoading,
            inputEnabled = chatId != null,
            sendEnabled = chatId != null && uiState.messageBody.isNotBlank(),
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
        )
    }
}
