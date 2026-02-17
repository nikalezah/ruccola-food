package kz.ruccola.food.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.repository.CustomerRepository
import kz.ruccola.food.repository.DishRepository
import kz.ruccola.food.ui.ApplyIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidDishVariantEditorScreen(
    dishId: Int,
    dishName: String,
    adminToken: String?,
    existing: DishVariantDto? = null,
    initialSelectedCustomerIds: Set<Int>? = null,
    onClose: () -> Unit,
    onSaved: (updatedOrCreated: DishVariantDto, selectedCustomerIds: Set<Int>) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dishRepo = remember { DishRepository() }
    val customerRepo = remember { CustomerRepository() }

    var description by remember(existing?.id) { mutableStateOf(existing?.description ?: "") }
    var allCustomers by remember { mutableStateOf<List<CustomerDto>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedCustomerIds by remember(existing?.id) { mutableStateOf(initialSelectedCustomerIds ?: emptySet()) }

    // Load customers if we have an admin token
    LaunchedEffect(adminToken) {
        if (adminToken != null) {
            loadError = null
            try {
                val res = customerRepo.getCustomers(adminToken)
                res.fold(onSuccess = { allCustomers = it }, onFailure = { e -> loadError = e.message })
            } catch (e: Exception) {
                loadError = e.message
            }
        }
    }

    fun doSave() {
        busy = true
        scope.launch {
            val variant = if (existing == null) {
                dishRepo.addVariant(dishId, description.trim())
            } else {
                dishRepo.updateVariant(dishId, existing.id, description.trim())
            }
            variant.fold(
                onSuccess = { v ->
                    val ids = selectedCustomerIds.toList()
                    val setRes = dishRepo.setVariantCustomers(dishId, v.id, ids)
                    setRes.fold(
                        onSuccess = {
                            busy = false
                            onSaved(v, selectedCustomerIds)
                        },
                        onFailure = { e ->
                            busy = false
                            Toast.makeText(
                                context,
                                e.message ?: "Error",
                                Toast.LENGTH_LONG,
                            ).show()
                        },
                    )
                },
                onFailure = { e ->
                    busy = false
                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
                },
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Новый вариант" else "Изменить вариант") },
                subtitle = { Text(dishName) },
                navigationIcon = {
                    IconButton(onClick = { if (!busy) onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ApplyIconButton(
                        onClick = { if (!busy && description.isNotBlank()) doSave() },
                        enabled = !busy && description.isNotBlank(),
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
                singleLine = false,
                minLines = 2,
                maxLines = 6,
            )

            // Customer picker (single select) with search
            if (adminToken == null) {
                Text(
                    text = "Sign in as admin to search and bind a customer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                AssistChip(
                    onClick = { selectedCustomerIds = emptySet() },
                    label = { Text("Unbind all") },
                    enabled = !busy,
                )
            } else {
                when {
                    loadError != null -> {
                        Text(loadError!!, color = MaterialTheme.colorScheme.error)
                    }

                    allCustomers == null -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Поиск клиентов по имени") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !busy,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                            shape = RoundedCornerShape(percent = 50),
                        )
                        val filtered = remember(query, allCustomers) {
                            val q = query.trim().lowercase()
                            if (q.isEmpty()) {
                                allCustomers!!
                            } else {
                                allCustomers!!.filter {
                                    it.firstName.lowercase().contains(q) || it.lastName.lowercase().contains(q)
                                }
                            }
                        }
                        if (filtered.isEmpty()) {
                            Text("No customers match your query", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                items(filtered, key = { it.id }) { c ->
                                    ListItem(
                                        headlineContent = { Text("${c.firstName} ${c.lastName}") },
                                        supportingContent = { Text(c.email) },
                                        leadingContent = {
                                            Checkbox(
                                                checked = selectedCustomerIds.contains(c.id),
                                                onCheckedChange = { checked ->
                                                    selectedCustomerIds =
                                                        if (checked) {
                                                            selectedCustomerIds + c.id
                                                        } else {
                                                            selectedCustomerIds -
                                                                c.id
                                                        }
                                                },
                                                enabled = !busy,
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(Modifier),
                                        trailingContent = null,
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
