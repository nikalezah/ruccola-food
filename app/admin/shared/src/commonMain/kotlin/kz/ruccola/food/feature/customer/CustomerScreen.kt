package kz.ruccola.food.feature.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.label_calories
import food.composeappadmin.generated.resources.no_customers_found
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.select_customer_hint
import food.composeappadmin.generated.resources.tab_customers
import kz.ruccola.food.api.CustomerDetailsDto
import kz.ruccola.food.feature.chat.ChatScreen
import kz.ruccola.food.ui.EmptyDetailPane
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.LocalWindowWidthClass
import kz.ruccola.food.ui.ResponsiveContainer
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.WindowWidthClass
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(onChatOpenChanged: (Boolean) -> Unit = {}, onUnreadChanged: (Boolean) -> Unit = {}) {
    val viewModel: CustomersViewModel = viewModel(factory = CustomersViewModel.factory())
    val uiState by viewModel.uiState.collectAsState()

    var selectedChatCustomer by remember { mutableStateOf<CustomerDetailsDto?>(null) }
    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerDetails by remember { mutableStateOf<CustomerDetailsDto?>(null) }

    val twoPane = LocalWindowWidthClass.current == WindowWidthClass.Expanded

    LaunchedEffect(Unit) { viewModel.loadCustomers() }

    LaunchedEffect(selectedChatCustomer) { onChatOpenChanged(selectedChatCustomer != null) }

    LaunchedEffect(uiState.chats) {
        val hasUnread =
            uiState.chats.values.any { chat ->
                chat.lastMessageId != null && chat.lastMessageId != chat.lastReadMessageId
            }
        onUnreadChanged(hasUnread)
    }

    DisposableEffect(Unit) { onDispose { onChatOpenChanged(false) } }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.tab_customers)) }) }) { padding
        ->
        if (twoPane) {
            Row(Modifier.fillMaxSize().padding(padding)) {
                CustomersList(
                    uiState = uiState,
                    selectedCustomer = selectedCustomerDetails,
                    onSelect = { selectedCustomerDetails = it },
                    onRetry = { viewModel.loadCustomers() },
                    modifier = Modifier.widthIn(max = 400.dp).fillMaxHeight(),
                )
                VerticalDivider()
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    val customer = selectedCustomerDetails
                    if (customer != null) {
                        ResponsiveContainer(maxContentWidth = 640.dp) {
                            Column(Modifier.fillMaxSize()) {
                                Text(
                                    text = "${customer.firstName} ${customer.lastName}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                )
                                CustomerDetailsContent(customer = customer, modifier = Modifier.fillMaxSize())
                            }
                        }
                    } else {
                        EmptyDetailPane(
                            icon = Icons.Outlined.Groups,
                            text = stringResource(Res.string.select_customer_hint),
                        )
                    }
                }
            }
        } else {
            CustomersList(
                uiState = uiState,
                selectedCustomer = null,
                onSelect = { selectedCustomerDetails = it },
                onRetry = { viewModel.loadCustomers() },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }

    if (!twoPane && selectedCustomerDetails != null) {
        CustomerDetailsScreen(customer = selectedCustomerDetails!!, onBack = { selectedCustomerDetails = null })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomersList(
    uiState: CustomersUiState,
    selectedCustomer: CustomerDetailsDto?,
    onSelect: (CustomerDetailsDto) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
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
                    Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
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

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                    items(uiState.customers) { c ->
                        ListItem(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(c) },
                            headlineContent = { Text("${c.firstName} ${c.lastName}") },
                            supportingContent = {
                                SingleLineText(
                                    "${c.address} - ${
                                        stringResource(
                                            Res.string.label_calories,
                                            c.plan?.calories?.toString() ?: "-",
                                        )
                                    }"
                                )
                            },
                            colors = listItemColorsFor(selected = selectedCustomer?.id == c.id),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun listItemColorsFor(selected: Boolean) =
    if (selected) {
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        ListItemDefaults.colors()
    }
