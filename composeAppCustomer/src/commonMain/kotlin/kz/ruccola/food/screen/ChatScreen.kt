package kz.ruccola.food.screen

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kz.ruccola.food.viewmodel.ChatViewModel
import org.jetbrains.compose.resources.stringResource

private fun parseUserId(token: String): Int? {
    if (!token.startsWith("dummy-token-")) return null
    return token.split("-").lastOrNull()?.toIntOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    token: String,
    onBack: (() -> Unit)? = null,
    viewModel: ChatViewModel = viewModel { ChatViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = remember(token) { parseUserId(token) }
    var messageBody by remember { mutableStateOf("") }
    val errorText = uiState.error?.let { stringResource(Res.string.error_prefix, it) }

    LaunchedEffect(token) {
        viewModel.loadChat(token)
    }

    LaunchedEffect(token) {
        viewModel.startPolling(token)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.chat_support_title)) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                )
            },
        ) { padding ->
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.loadChat(token, isRefreshing = true) },
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                ChatUi(
                    messages = uiState.messages,
                    currentUserId = currentUserId,
                    messageBody = messageBody,
                    onMessageBodyChange = { messageBody = it },
                    onSendMessage = {
                        viewModel.sendMessage(token, messageBody)
                        messageBody = ""
                    },
                    placeholder = stringResource(Res.string.chat_placeholder),
                    emptyText = stringResource(Res.string.chat_empty),
                    errorText = errorText,
                    isLoading = uiState.isLoading,
                    inputEnabled = true,
                    sendEnabled = true,
                    locale = "ru-RU", // Default locale as used in RuStrings, ideally this should also be a resource or dynamic
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                )
            }
        }
    }
}
