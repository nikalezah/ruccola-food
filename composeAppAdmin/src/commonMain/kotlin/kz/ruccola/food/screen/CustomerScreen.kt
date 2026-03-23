package kz.ruccola.food.screen

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
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.chat_open
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.label_calories
import food.composeappadmin.generated.resources.no_customers_found
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.tab_customers
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.CustomersViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    onChatOpenChanged: (Boolean) -> Unit = {},
    onUnreadChanged: (Boolean) -> Unit = {},
) {
    val viewModel: CustomersViewModel = viewModel { CustomersViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var selectedChatCustomer by remember { mutableStateOf<CustomerDto?>(null) }
    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerDetails by remember { mutableStateOf<CustomerDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
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
                title = { Text(stringResource(Res.string.tab_customers)) },
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
                            stringResource(Res.string.error_prefix, uiState.error ?: "[?]"),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadCustomers() }) { Text(stringResource(Res.string.retry)) }
                    }
                }

                uiState.customers.isEmpty() && !uiState.isLoading -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.no_customers_found))
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
                                        "${c.address} - ${
                                            stringResource(Res.string.label_calories, c.calories?.toString() ?: "-")
                                        }",
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
                                                imageVector = Icons.Outlined.Chat,
                                                contentDescription = stringResource(Res.string.chat_open),
                                            )
                                        }
                                    }
                                    if (isUnread) {
                                        BadgedBox(badge = { Badge { Text("1") } }) { chatButton() }
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
            chatId = selectedChatId,
            customerName = "${selectedChatCustomer?.firstName} ${selectedChatCustomer?.lastName}",
            onBack = {
                selectedChatCustomer = null
                selectedChatId = null
                viewModel.loadCustomers()
            },
        )
    }
}
