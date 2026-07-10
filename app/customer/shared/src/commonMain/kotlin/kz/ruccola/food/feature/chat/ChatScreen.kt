package kz.ruccola.food.feature.chat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.chat_empty
import food.composeappcustomer.generated.resources.chat_placeholder
import food.composeappcustomer.generated.resources.chat_support_title
import food.composeappcustomer.generated.resources.error_prefix
import kz.ruccola.food.ui.ChatUi
import kz.ruccola.food.ui.Icons
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: (() -> Unit)? = null, viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsState()
    val errorText = uiState.error?.let { stringResource(Res.string.error_prefix, it) }

    LaunchedEffect(Unit) {
        viewModel.loadChat()
        viewModel.startPolling()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.chat_support_title)) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    },
                )
            }
        ) {
            ChatUi(
                messages = uiState.messages,
                messageBody = uiState.messageBody,
                onMessageBodyChange = { viewModel.onMessageBodyChange(it) },
                onSendMessage = { viewModel.sendMessage() },
                placeholder = stringResource(Res.string.chat_placeholder),
                emptyText = stringResource(Res.string.chat_empty),
                errorText = errorText,
                isLoading = uiState.isLoading,
                inputEnabled = true,
                sendEnabled = uiState.messageBody.isNotBlank(),
                locale = "ru-RU",
                modifier = Modifier.fillMaxSize().padding(12.dp),
            )
        }
    }
}
