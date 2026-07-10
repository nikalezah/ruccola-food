package kz.ruccola.food.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.chat_empty
import food.composeappcustomer.generated.resources.chat_support_title
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.tab_chat
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SingleLineText
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatOpenChanged: (Boolean) -> Unit = {},
    onUnreadChanged: (Boolean) -> Unit = {},
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isChatOpen by remember { mutableStateOf(false) }
    val errorText = uiState.error?.let { stringResource(Res.string.error_prefix, it) }

    LaunchedEffect(Unit) { viewModel.loadChat() }
    LaunchedEffect(isChatOpen) { onChatOpenChanged(isChatOpen) }
    LaunchedEffect(uiState.chat?.lastMessageId, uiState.chat?.lastReadMessageId) {
        val chat = uiState.chat
        val isUnread = chat?.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
        onUnreadChanged(isUnread)
    }
    DisposableEffect(Unit) { onDispose { onChatOpenChanged(false) } }

    Box(Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            errorText != null -> {
                Text(errorText, modifier = Modifier.align(Alignment.Center))
            }

            uiState.chat == null -> {
                Text(stringResource(Res.string.chat_empty), modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                val chat = uiState.chat
                val isUnread = chat?.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
                val messageText = uiState.lastMessage?.body ?: stringResource(Res.string.chat_empty)

                Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.tab_chat)) }) }) { padding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(Res.string.chat_support_title)) },
                                supportingContent = { SingleLineText(messageText) },
                                trailingContent = {
                                    val icon: @Composable () -> Unit = {
                                        Icon(Icons.Outlined.Chat, contentDescription = null)
                                    }
                                    if (isUnread) {
                                        BadgedBox(badge = { Badge() }) { icon() }
                                    } else {
                                        icon()
                                    }
                                },
                                modifier = Modifier.clickable(onClick = { isChatOpen = true }),
                            )
                        }
                    }
                }
            }
        }

        if (isChatOpen) {
            ChatScreen(onBack = { isChatOpen = false })
        }
    }
}
