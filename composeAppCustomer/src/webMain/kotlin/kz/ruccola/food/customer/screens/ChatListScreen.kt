package kz.ruccola.food.customer.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.LocalStrings
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatDto
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.ui.Badge
import kz.ruccola.food.ui.BadgedBox
import kz.ruccola.food.ui.SingleLineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    token: String,
    onChatOpenChanged: (Boolean) -> Unit,
    onUnreadChanged: (Boolean) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatApi() }
    var chat by remember { mutableStateOf<ChatDto?>(null) }
    var lastMessage by remember { mutableStateOf<MessageDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isChatOpen by remember { mutableStateOf(false) }
    val errorText = error?.let { strings.errorPrefix.replace("%s", it) }

    fun loadChat() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val loadedChat = api.getMyChat(token)
                chat = loadedChat
                val afterId = loadedChat.lastMessageId?.minus(1)
                lastMessage = api.getMessages(
                    token = token,
                    chatId = loadedChat.id,
                    afterId = afterId,
                    limit = 1,
                ).lastOrNull()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(token) {
        loadChat()
    }
    LaunchedEffect(isChatOpen) {
        onChatOpenChanged(isChatOpen)
    }
    LaunchedEffect(chat?.lastMessageId, chat?.lastReadMessageId) {
        val isUnread = chat?.lastMessageId != null && chat?.lastMessageId != chat?.lastReadMessageId
        onUnreadChanged(isUnread)
    }
    DisposableEffect(Unit) {
        onDispose { onChatOpenChanged(false) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.screenChatListTitle) },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                errorText != null -> {
                    Text(errorText, modifier = Modifier.align(Alignment.Center))
                }

                chat == null -> {
                    Text(strings.chatEmpty, modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    val isUnread = chat?.lastMessageId != null && chat?.lastMessageId != chat?.lastReadMessageId
                    val messageText = lastMessage?.body ?: strings.chatEmpty
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
    }

    if (isChatOpen) {
        ChatScreen(
            token = token,
            onBack = { isChatOpen = false },
        )
    }
}
