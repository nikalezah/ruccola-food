package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.cancel
import food.composeappadmin.generated.resources.close
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.description
import food.composeappadmin.generated.resources.edit
import food.composeappadmin.generated.resources.edit_description
import food.composeappadmin.generated.resources.edit_dish
import food.composeappadmin.generated.resources.edit_name
import food.composeappadmin.generated.resources.images
import food.composeappadmin.generated.resources.label_customers
import food.composeappadmin.generated.resources.name
import food.composeappadmin.generated.resources.new_dish
import food.composeappadmin.generated.resources.no_customers_bound
import food.composeappadmin.generated.resources.save
import food.composeappadmin.generated.resources.variants
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.provideAdminToken
import kz.ruccola.food.ui.SquareImagesCarousel200
import kz.ruccola.food.ui.SwipeToRemove
import kz.ruccola.food.viewmodel.DishEditorViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishEditorScreen(
    initialDish: DishDto?,
    onClose: () -> Unit,
    token: String? = null,
) {
    val effectiveToken = token ?: provideAdminToken()
    val viewModel = remember(initialDish, effectiveToken) { DishEditorViewModel(initialDish, effectiveToken) }
    val uiState by viewModel.uiState.collectAsState()

    var variantEditorVisible by remember { mutableStateOf(false) }
    var editingVariant by remember { mutableStateOf<DishVariantDto?>(null) }
    var imageEditorVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.dish == null) Res.string.new_dish else Res.string.edit_dish))
                },
                navigationIcon = {
                    IconButton(onClick = { if (!uiState.isBusy) onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.close))
                    }
                },
                actions = {
                    if (uiState.dish == null) {
                        FilledIconButton(
                            onClick = { viewModel.saveDish() },
                            enabled = uiState.name.isNotBlank() && !uiState.isBusy,
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                },
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(16.dp)) {
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (uiState.dish == null) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text(stringResource(Res.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isBusy,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text(stringResource(Res.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !uiState.isBusy,
                )
                Spacer(Modifier.height(12.dp))
            } else {
                var showEditName by remember { mutableStateOf(false) }
                var showEditDescription by remember { mutableStateOf(false) }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.name.ifBlank { "(no name)" },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showEditName = true }, enabled = !uiState.isBusy) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit name")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.description.ifBlank { "(no description)" },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showEditDescription = true }, enabled = !uiState.isBusy) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit description")
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (showEditName) {
                    var temp by remember { mutableStateOf(uiState.name) }
                    AlertDialog(
                        onDismissRequest = { showEditName = false },
                        confirmButton = {
                            TextButton(enabled = temp.isNotBlank() && !uiState.isBusy, onClick = {
                                showEditName = false
                                viewModel.updateDishName(temp)
                            }) { Text(stringResource(Res.string.save)) }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showEditName = false
                            }) { Text(stringResource(Res.string.cancel)) }
                        },
                        title = { Text(stringResource(Res.string.edit_name)) },
                        text = {
                            OutlinedTextField(
                                value = temp,
                                onValueChange = { temp = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                        },
                    )
                }

                if (showEditDescription) {
                    var temp by remember { mutableStateOf(uiState.description) }
                    AlertDialog(
                        onDismissRequest = { showEditDescription = false },
                        confirmButton = {
                            TextButton(enabled = !uiState.isBusy, onClick = {
                                showEditDescription = false
                                viewModel.updateDishDescription(temp)
                            }) { Text(stringResource(Res.string.save)) }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showEditDescription = false
                            }) { Text(stringResource(Res.string.cancel)) }
                        },
                        title = { Text(stringResource(Res.string.edit_description)) },
                        text = {
                            OutlinedTextField(
                                value = temp,
                                onValueChange = { temp = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                            )
                        },
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.images), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { imageEditorVisible = true }, enabled = !uiState.isBusy) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit))
                    }
                }
                Spacer(Modifier.height(8.dp))
                SquareImagesCarousel200(imageUrls = uiState.dish?.images?.map { it.url } ?: emptyList())

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.variants),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(enabled = !uiState.isBusy, onClick = {
                        editingVariant = null
                        variantEditorVisible = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add))
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (!uiState.variantsLoaded) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.variants.forEach { v ->
                            key(v.id) {
                                SwipeToRemove(
                                    Icons.Default.Delete,
                                    stringResource(Res.string.delete),
                                    { viewModel.deleteVariant(v) },
                                    CardDefaults.outlinedShape,
                                    enabled = !uiState.isBusy,
                                ) {
                                    OutlinedCard(
                                        onClick = {
                                            editingVariant = v
                                            variantEditorVisible = true
                                        },
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(Modifier.padding(12.dp).weight(1f)) {
                                                Text(v.description, style = MaterialTheme.typography.bodyLarge)
                                                Spacer(Modifier.height(4.dp))

                                                val ids = uiState.variantCustomers[v.id]
                                                when {
                                                    uiState.customersLoading && ids == null -> {
                                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                                    }

                                                    ids.isNullOrEmpty() -> {
                                                        Text(
                                                            stringResource(Res.string.no_customers_bound),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }

                                                    else -> {
                                                        val names = ids.map { cid ->
                                                            val c = uiState.allCustomers?.find { it.id == cid }
                                                            if (c != null) "${c.firstName} ${c.lastName}" else "ID $cid"
                                                        }
                                                        val line = names.joinToString(", ")
                                                        Text(
                                                            stringResource(Res.string.label_customers, line),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isBusy) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (variantEditorVisible && uiState.dish != null && effectiveToken != null) {
        DishVariantEditorScreen(
            dishId = uiState.dish!!.id,
            dishName = uiState.dish!!.name,
            token = effectiveToken,
            existing = editingVariant,
            initialCustomerIds = editingVariant?.let { uiState.variantCustomers[it.id] },
            onClose = { variantEditorVisible = false },
            onSaved = { _, _ ->
                variantEditorVisible = false
                viewModel.loadVariants()
            },
        )
    }

    if (imageEditorVisible && uiState.dish != null) {
        DishImagesEditorScreen(
            dish = uiState.dish!!,
            onClose = { imageEditorVisible = false },
            onSaved = { updated -> viewModel.onDishUpdated(updated) },
        )
    }
}
