package kz.ruccola.food.feature.chat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.back_to_login
import food.composeappadmin.generated.resources.chat_empty
import food.composeappadmin.generated.resources.chat_message_placeholder
import food.composeappadmin.generated.resources.error_prefix
import kz.ruccola.food.ui.ChatUi
import kz.ruccola.food.ui.Icons
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: Int?,
    customerName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.factory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorText = uiState.error?.let { stringResource(Res.string.error_prefix, it) }

    LaunchedEffect(chatId) { viewModel.setChatId(chatId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customerName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_to_login),
                        )
                    }
                },
            )
        }
    ) { padding ->
        ChatUi(
            messages = uiState.messages,
            messageBody = uiState.messageBody,
            onMessageBodyChange = { viewModel.onMessageBodyChange(it) },
            onSendMessage = { viewModel.sendMessage() },
            placeholder = stringResource(Res.string.chat_message_placeholder),
            emptyText = stringResource(Res.string.chat_empty),
            errorText = errorText,
            isLoading = uiState.isLoading,
            inputEnabled = chatId != null,
            sendEnabled = chatId != null && uiState.messageBody.isNotBlank(),
            locale = "ru-RU",
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
        )
    }
}
