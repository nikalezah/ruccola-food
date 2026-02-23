package kz.ruccola.food.admin.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.Strings
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.api.DishVariantSaveDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishVariantEditorScreen(
    dishId: Int,
    dishName: String,
    token: String,
    existing: DishVariantDto? = null,
    initialCustomerIds: Set<Int>? = null,
    onClose: () -> Unit,
    onSaved: () -> Unit,
) {
    val dishApi = remember { DishApi() }
    val customerApi = remember { CustomerApi() }
    val scope = rememberCoroutineScope()

    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    var allCustomers by remember { mutableStateOf<List<CustomerDto>?>(null) }
    var selectedCustomerIds by remember(existing, initialCustomerIds) {
        mutableStateOf(initialCustomerIds ?: existing?.customerIds?.toSet() ?: emptySet())
    }

    LaunchedEffect(Unit) {
        try {
            allCustomers = customerApi.getAll(token)
        } catch (e: Exception) {
            error = e.message
        }
    }

    fun save() {
        scope.launch {
            busy = true
            error = null
            try {
                val variant = if (existing == null) {
                    dishApi.createVariant(dishId, DishVariantSaveDto(description = description.trim()))
                } else {
                    dishApi.updateVariant(dishId, existing.id, DishVariantSaveDto(description = description.trim()))
                }

                dishApi.setVariantCustomers(dishId, variant.id, selectedCustomerIds.toList())
                onSaved()
            } catch (e: Exception) {
                error = e.message ?: Strings.saveFailed
            } finally {
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) Strings.newVariant else Strings.editVariant) },
                // subtitle = { Text(dishName) }, // TopAppBar doesn't have a subtitle in M3
                navigationIcon = {
                    IconButton(onClick = { if (!busy) onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledIconButton(onClick = { save() }, enabled = description.isNotBlank() && !busy) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Text(dishName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(Strings.variantDescription) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !busy,
            )

            Spacer(Modifier.height(24.dp))
            Text(Strings.tabCustomers, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(Strings.searchCustomers) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !busy,
            )

            Spacer(Modifier.height(8.dp))

            if (allCustomers == null && error == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                val filtered = allCustomers?.filter {
                    val q = query.trim().lowercase()
                    q.isEmpty() || it.firstName.lowercase().contains(q) ||
                        it.lastName.lowercase().contains(q) || it.email.lowercase().contains(q)
                } ?: emptyList()

                if (filtered.isEmpty() && query.isNotEmpty()) {
                    Text(Strings.noCustomersFound, modifier = Modifier.padding(vertical = 8.dp))
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { customer ->
                        val isSelected = selectedCustomerIds.contains(customer.id)
                        ListItem(
                            headlineContent = { Text("${customer.firstName} ${customer.lastName}") },
                            supportingContent = { Text(customer.email) },
                            leadingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedCustomerIds = if (checked) {
                                            selectedCustomerIds + customer.id
                                        } else {
                                            selectedCustomerIds - customer.id
                                        }
                                    },
                                    enabled = !busy,
                                )
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }

            if (busy) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
