package kz.ruccola.food.admin.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatListItemDto
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.web.common.ui.Badge
import kz.ruccola.food.web.common.ui.BadgedBox
import kz.ruccola.food.web.common.ui.SingleLineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    token: String,
    onChatOpenChanged: (Boolean) -> Unit,
    onUnreadChanged: (Boolean) -> Unit,
) {
    var customers by remember { mutableStateOf<List<CustomerDto>>(emptyList()) }
    var chats by remember { mutableStateOf<Map<Int, ChatListItemDto>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedChatCustomer by remember { mutableStateOf<CustomerDto?>(null) }
    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerDetails by remember { mutableStateOf<CustomerDto?>(null) }

    val scope = rememberCoroutineScope()
    val api = remember { CustomerApi() }
    val chatApi = remember { ChatApi() }

    fun loadCustomers() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val loadedCustomers = api.getAll(token)
                val chatItems = chatApi.getChats(token)
                customers = loadedCustomers
                chats = chatItems.associateBy { it.customerId }
            } catch (e: Exception) {
                error = e.message ?: Strings.error
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadCustomers() }
    LaunchedEffect(selectedChatCustomer) {
        onChatOpenChanged(selectedChatCustomer != null)
    }
    LaunchedEffect(chats) {
        val hasUnread = chats.values.any { chat ->
            chat.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
        }
        onUnreadChanged(hasUnread)
    }
    DisposableEffect(Unit) {
        onDispose { onChatOpenChanged(false) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(Strings.tabCustomers) },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            Strings.errorPrefix.replace("%s", error ?: "[?]"),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { loadCustomers() }) { Text(Strings.retry) }
                    }
                }

                customers.isEmpty() -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(Strings.noCustomersFound)
                    }
                }

                selectedChatCustomer == null && selectedCustomerDetails == null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        items(customers) { c ->
                            val chat = chats[c.id]
                            val isUnread = chat?.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
                            ListItem(
                                modifier = Modifier.fillMaxWidth().clickable { selectedCustomerDetails = c },
                                headlineContent = { Text("${c.firstName} ${c.lastName}") },
                                supportingContent = {
                                    SingleLineText("${c.address} - Calories: ${c.calories?.toString() ?: "-"}")
                                },
                                trailingContent = {
                                    val onClick = {
                                        selectedChatCustomer = c
                                        selectedChatId = chats[c.id]?.id
                                    }
                                    val chatButton: @Composable () -> Unit = {
                                        IconButton(onClick = onClick) {
                                            Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "Chat")
                                        }
                                    }
                                    if (isUnread) {
                                        BadgedBox(badge = { Badge() }, badgeOffset = DpOffset((-6).dp, 6.dp)) {
                                            chatButton()
                                        }
                                    } else {
                                        chatButton()
                                    }
                                },
                            )
                        }
                    }
                }

                else -> {
                    Unit
                }
            }
        }
    }

    if (selectedCustomerDetails != null) {
        CustomerDetailsScreen(
            customer = selectedCustomerDetails!!,
            onBack = { selectedCustomerDetails = null },
        )
    }

    if (selectedChatCustomer != null) {
        ChatScreen(
            token = token,
            chatId = selectedChatId,
            customerName = "${selectedChatCustomer?.firstName} ${selectedChatCustomer?.lastName}",
            onBack = {
                selectedChatCustomer = null
                selectedChatId = null
            },
        )
    }
}
