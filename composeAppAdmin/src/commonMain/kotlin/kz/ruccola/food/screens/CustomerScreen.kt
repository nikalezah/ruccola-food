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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.ruccola.food.Strings
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.ui.Badge
import kz.ruccola.food.ui.BadgedBox
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.CustomersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    token: String,
    onChatOpenChanged: (Boolean) -> Unit = {},
    onUnreadChanged: (Boolean) -> Unit = {},
) {
    val viewModel: CustomersViewModel = viewModel { CustomersViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var selectedChatCustomer by remember { mutableStateOf<CustomerDto?>(null) }
    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerDetails by remember { mutableStateOf<CustomerDto?>(null) }

    LaunchedEffect(token) {
        viewModel.loadCustomers(token)
    }

    LaunchedEffect(selectedChatCustomer) {
        onChatOpenChanged(selectedChatCustomer != null)
    }

    LaunchedEffect(uiState.chats) {
        val hasUnread = uiState.chats.values.any { chat ->
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
                uiState.isLoading && uiState.customers.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            Strings.errorPrefix.replace("%s", uiState.error ?: "[?]"),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadCustomers(token) }) { Text(Strings.retry) }
                    }
                }

                uiState.customers.isEmpty() && !uiState.isLoading -> {
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
                        items(uiState.customers) { c ->
                            val chat = uiState.chats[c.id]
                            val isUnread = chat?.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
                            ListItem(
                                modifier = Modifier.fillMaxWidth().clickable { selectedCustomerDetails = c },
                                headlineContent = { Text("${c.firstName} ${c.lastName}") },
                                supportingContent = {
                                    SingleLineText(
                                        "${c.address} - ${Strings.labelCalories.replace(
                                            "%s",
                                            c.calories?.toString() ?: "-",
                                        )}",
                                    )
                                },
                                trailingContent = {
                                    val onClick = {
                                        selectedChatCustomer = c
                                        selectedChatId = uiState.chats[c.id]?.id
                                    }
                                    val chatButton: @Composable () -> Unit = {
                                        IconButton(onClick = onClick) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                                contentDescription = Strings.chatOpen,
                                            )
                                        }
                                    }
                                    if (isUnread) {
                                        BadgedBox(
                                            badge = {
                                                Badge {
                                                    Text("1")
                                                }
                                            },
                                            badgeOffset = DpOffset((-6).dp, 6.dp),
                                        ) {
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
                viewModel.loadCustomers(token)
            },
        )
    }
}
