package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.close
import food.composeappadmin.generated.resources.edit_variant
import food.composeappadmin.generated.resources.new_variant
import food.composeappadmin.generated.resources.no_customers_found
import food.composeappadmin.generated.resources.save
import food.composeappadmin.generated.resources.search_customers
import food.composeappadmin.generated.resources.tab_customers
import food.composeappadmin.generated.resources.variant_description
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.ui.ApplyIconButton
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.viewmodel.DishVariantViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishVariantEditorScreen(
    dishId: Int,
    dishName: String,
    existing: DishVariantDto? = null,
    initialCustomerIds: Set<Int>? = null,
    onClose: () -> Unit,
    onSaved: (DishVariantDto, Set<Int>) -> Unit,
) {
    val viewModel = remember(dishId, existing) {
        DishVariantViewModel(dishId, existing, initialCustomerIds)
    }
    val uiState by viewModel.uiState.collectAsState()
    val savedVariant = uiState.savedVariant

    LaunchedEffect(savedVariant) {
        if (savedVariant != null) {
            onSaved(savedVariant, uiState.selectedCustomerIds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (existing == null) Res.string.new_variant else Res.string.edit_variant))
                },
                navigationIcon = {
                    IconButton(onClick = { if (!uiState.isBusy) onClose() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(Res.string.close))
                    }
                },
                actions = {
                    ApplyIconButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.hasChanges && uiState.description.isNotBlank() && !uiState.isBusy,
                        contentDescription = stringResource(Res.string.save),
                    )
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Text(dishName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(stringResource(Res.string.variant_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !uiState.isBusy,
            )

            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.tab_customers), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text(stringResource(Res.string.search_customers)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isBusy,
            )

            Spacer(Modifier.height(8.dp))

            if (uiState.allCustomers == null && uiState.error == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                val filtered = uiState.allCustomers?.filter {
                    val q = uiState.searchQuery.trim().lowercase()
                    q.isEmpty() || it.firstName.lowercase().contains(q) ||
                        it.lastName.lowercase().contains(q) || it.email.lowercase().contains(q)
                } ?: emptyList()

                if (filtered.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                    Text(stringResource(Res.string.no_customers_found), modifier = Modifier.padding(vertical = 8.dp))
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { customer ->
                        val isSelected = uiState.selectedCustomerIds.contains(customer.id)
                        ListItem(
                            headlineContent = { Text("${customer.firstName} ${customer.lastName}") },
                            supportingContent = { Text(customer.email) },
                            leadingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleCustomerSelection(customer.id) },
                                    enabled = !uiState.isBusy,
                                )
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }

            if (uiState.isBusy) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
