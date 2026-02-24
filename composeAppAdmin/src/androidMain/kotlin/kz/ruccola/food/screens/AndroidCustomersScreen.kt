package kz.ruccola.food.screens

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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.R
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatListItemDto
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidCustomersScreen(token: String) {
    val errorFallback = stringResource(R.string.error_generic)
    val scope = rememberCoroutineScope()
    val api = remember { CustomerApi() }
    val chatApi = remember { ChatApi() }
    var customers by remember { mutableStateOf<List<CustomerDto>>(emptyList()) }
    var chats by remember { mutableStateOf<Map<Int, ChatListItemDto>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedChatCustomer by remember { mutableStateOf<CustomerDto?>(null) }
    var selectedChatId by remember { mutableStateOf<Int?>(null) }

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
                error = e.message ?: errorFallback
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCustomers()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.customers_title)) },
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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.error_prefix,
                                error ?: "[?]",
                            ),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { loadCustomers() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }

                customers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(R.string.no_customers_found))
                    }
                }

                selectedChatCustomer == null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        items(customers) { customer ->
                            val chat = chats[customer.id]
                            val isUnread = chat?.lastMessageId != null &&
                                chat.lastMessageId != chat.lastReadMessageId
                            val calories = customer.calories?.toString() ?: "-"
                            val caloriesText = stringResource(
                                R.string.label_calories,
                                calories,
                            )
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedChatCustomer = customer
                                        selectedChatId = chats[customer.id]?.id
                                    },
                                headlineContent = {
                                    Text("${customer.firstName} ${customer.lastName}")
                                },
                                supportingContent = {
                                    Text(
                                        text = "${customer.address} - $caloriesText",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                trailingContent = {
                                    val onClick = {
                                        selectedChatCustomer = customer
                                        selectedChatId = chats[customer.id]?.id
                                    }
                                    val unreadCount = if (isUnread) 1 else 0
                                    IconButton(onClick = onClick) {
                                        BadgedBox(
                                            badge = {
                                                if (unreadCount > 0) {
                                                    Badge {
                                                        Text(unreadCount.toString())
                                                    }
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                                contentDescription = stringResource(R.string.chat_open),
                                            )
                                        }
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

    if (selectedChatCustomer != null) {
        ChatScreen(
            token = token,
            chatId = selectedChatId,
            customerName = "${selectedChatCustomer?.firstName} " +
                selectedChatCustomer?.lastName,
            onBack = {
                selectedChatCustomer = null
                selectedChatId = null
            },
        )
    }
}
