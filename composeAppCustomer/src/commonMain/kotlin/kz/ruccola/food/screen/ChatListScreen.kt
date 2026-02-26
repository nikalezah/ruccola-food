package kz.ruccola.food.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
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
import kz.ruccola.food.LocalStrings
import kz.ruccola.food.ui.Badge
import kz.ruccola.food.ui.BadgedBox
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    token: String,
    onChatOpenChanged: (Boolean) -> Unit = {},
    onUnreadChanged: (Boolean) -> Unit = {},
    viewModel: ChatViewModel = viewModel { ChatViewModel() },
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()
    var isChatOpen by remember { mutableStateOf(false) }
    val errorText = uiState.error?.let { strings.errorPrefix.replace("%s", it) }

    LaunchedEffect(token) {
        viewModel.loadChat(token)
    }
    LaunchedEffect(isChatOpen) {
        onChatOpenChanged(isChatOpen)
    }
    LaunchedEffect(uiState.chat?.lastMessageId, uiState.chat?.lastReadMessageId) {
        val chat = uiState.chat
        val isUnread = chat?.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
        onUnreadChanged(isUnread)
    }
    DisposableEffect(Unit) {
        onDispose { onChatOpenChanged(false) }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            errorText != null -> {
                Text(errorText, modifier = Modifier.align(Alignment.Center))
            }

            uiState.chat == null -> {
                Text(strings.chatEmpty, modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                val chat = uiState.chat
                val isUnread = chat?.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
                val messageText = uiState.lastMessage?.body ?: strings.chatEmpty

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(strings.screenChatListTitle) },
                        )
                    },
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text(strings.chatSupportTitle) },
                                supportingContent = { SingleLineText(messageText) },
                                trailingContent = {
                                    val icon: @Composable () -> Unit = {
                                        Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null)
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
            ChatScreen(
                token = token,
                onBack = { isChatOpen = false },
            )
        }
    }
}
