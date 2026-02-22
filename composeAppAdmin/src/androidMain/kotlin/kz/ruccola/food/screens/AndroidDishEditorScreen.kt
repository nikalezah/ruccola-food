package kz.ruccola.food.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.repository.DishRepository
import kz.ruccola.food.repository.FileRepository
import kz.ruccola.food.ui.ApplyIconButton
import kz.ruccola.food.ui.SquareImagesCarousel200
import kz.ruccola.food.ui.SwipeToRemove

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidDishEditorScreen(
    initialDish: DishDto?,
    onClose: () -> Unit,
    adminToken: String? = null,
) {
    val context = LocalContext.current
    val dishRepo = remember { DishRepository() }
    val fileRepo = remember { FileRepository() }
    val scope = rememberCoroutineScope()

    var dishState by remember { mutableStateOf(initialDish) }
    var name by remember { mutableStateOf(initialDish?.name ?: "") }
    var description by remember { mutableStateOf(initialDish?.description ?: "") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showImagesEditor by remember { mutableStateOf(false) }
    // State for a dedicated Variant Editor screen
    var variantEditorOpen by remember { mutableStateOf(false) }
    var variantEditorExisting by remember { mutableStateOf<DishVariantDto?>(null) }
    var variantEditorInitialCustomerIds by remember { mutableStateOf<Set<Int>?>(null) }

    // When the editor saves, we capture the result here and process it when returning
    data class PendingVariant(
        val item: DishVariantDto,
        val selectedCustomerIds: Set<Int>,
        val isCreate: Boolean,
    )

    var variantSavePending by remember { mutableStateOf<PendingVariant?>(null) }

    // Navigate to a dedicated image editor screen instead of a modal
    if (showImagesEditor && dishState != null) {
        AndroidDishImagesEditorScreen(
            dish = dishState!!,
            onBack = { showImagesEditor = false },
            onDishUpdated = { updated ->
                dishState = updated
                // Close the screen after a successful update if needed; keep open to allow multiple edits
            },
        )
        return
    }

    // Navigate to the variant editor screen instead of embedding it in the scrollable content
    if (variantEditorOpen && dishState != null) {
        AndroidDishVariantEditorScreen(
            dishId = dishState!!.id,
            dishName = name,
            adminToken = adminToken,
            existing = variantEditorExisting,
            initialSelectedCustomerIds = variantEditorInitialCustomerIds,
            onClose = {
                variantEditorOpen = false
                variantEditorExisting = null
                variantEditorInitialCustomerIds = null
            },
            onSaved = { updatedOrCreated, selectedCustomerIds ->
                // Capture the result to be applied when we return to this screen
                val isCreate = (variantEditorExisting == null)
                variantSavePending = PendingVariant(updatedOrCreated, selectedCustomerIds, isCreate)
                variantEditorOpen = false
                variantEditorExisting = null
                variantEditorInitialCustomerIds = null
            },
        )
        return
    }

    // Handle system back gesture and back button to behave the same
    BackHandler(enabled = true) {
        if (!busy) onClose()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && dishState != null) {
            busy = true
            error = null
            // Using LaunchedEffect as a simple way to call suspending ops from callback
            // Alternatively, could use rememberCoroutineScope.launch
            scope.launch {
                val uploadRes = fileRepo.upload(context, uri)
                uploadRes.fold(onSuccess = { uploaded ->
                    val current = dishState!!
                    val existing = current.images.map { it.fileId }
                    val updatedList = existing + uploaded.id
                    val res = dishRepo.updateDishImages(current.id, updatedList)
                    res.fold(onSuccess = { updatedDish ->
                        dishState = updatedDish
                        busy = false
                        Toast.makeText(context, "Image added", Toast.LENGTH_SHORT).show()
                    }, onFailure = { e ->
                        busy = false
                        error = e.message
                    })
                }, onFailure = { e ->
                    busy = false
                    error = e.message
                })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (dishState == null) "Новое блюдо" else "Изменить блюдо") },
                navigationIcon = {
                    IconButton(onClick = { if (!busy) onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (dishState == null) {
                        ApplyIconButton(
                            onClick = {
                                busy = true
                                error = null
                                scope.launch {
                                    val res = dishRepo.createDish(name.trim(), description.trim())
                                    res.fold(onSuccess = { created ->
                                        dishState = created
                                        busy = false
                                        Toast.makeText(context, "Dish created", Toast.LENGTH_SHORT).show()
                                    }, onFailure = { e ->
                                        error = e.message
                                        busy = false
                                    })
                                }
                            },
                            enabled = name.isNotBlank() && !busy,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(16.dp)) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            // Editing for name/description: for an existing dish, show text with edit icon and modal; for a new dish, allow inline entry
            var showEditName by remember { mutableStateOf(false) }
            var showEditDescription by remember { mutableStateOf(false) }

            if (dishState == null) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                Spacer(Modifier.height(12.dp))
            } else {
                // Name row
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = name.ifBlank { "(no name)" },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showEditName = true }, enabled = !busy) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit name")
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Description row
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = description.ifBlank { "(no description)" },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showEditDescription = true }, enabled = !busy) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit description")
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (showEditName) {
                    var temp by remember { mutableStateOf(name) }
                    AlertDialog(
                        onDismissRequest = { showEditName = false },
                        confirmButton = {
                            TextButton(enabled = temp.isNotBlank() && !busy, onClick = {
                                showEditName = false
                                if (dishState != null) {
                                    busy = true
                                    error = null
                                    scope.launch {
                                        val res = dishRepo.updateDish(dishState!!.id, temp.trim(), null)
                                        res.fold(onSuccess = { updated ->
                                            dishState = updated
                                            name = updated.name
                                            busy = false
                                            Toast.makeText(context, "Name updated", Toast.LENGTH_SHORT).show()
                                        }, onFailure = { e ->
                                            error = e.message
                                            busy = false
                                        })
                                    }
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showEditName = false }) { Text("Cancel") } },
                        title = { Text("Edit name") },
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
                    var temp by remember { mutableStateOf(description) }
                    AlertDialog(
                        onDismissRequest = { showEditDescription = false },
                        confirmButton = {
                            TextButton(enabled = !busy, onClick = {
                                showEditDescription = false
                                if (dishState != null) {
                                    busy = true
                                    error = null
                                    scope.launch {
                                        val res = dishRepo.updateDish(dishState!!.id, null, temp.trim())
                                        res.fold(onSuccess = { updated ->
                                            dishState = updated
                                            description = updated.description
                                            busy = false
                                            Toast.makeText(context, "Description updated", Toast.LENGTH_SHORT).show()
                                        }, onFailure = { e ->
                                            error = e.message
                                            busy = false
                                        })
                                    }
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showEditDescription = false }) { Text("Cancel") } },
                        title = { Text("Edit description") },
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
            }

            if (dishState != null) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Images", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    // Edit button aligned with the title
                    IconButton(onClick = { showImagesEditor = true }, enabled = !busy) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit images")
                    }
                }
                Spacer(Modifier.height(8.dp))

                val images = dishState!!.images
                if (images.isNotEmpty()) {
                    SquareImagesCarousel200(images.map { it.url })
                    Spacer(Modifier.height(8.dp))
                }

                // Variants UI moved to the bottom after images
                var variants by remember { mutableStateOf<List<DishVariantDto>>(emptyList()) }
                var variantsLoaded by remember { mutableStateOf(false) }
                // Map of variantId -> set of customer IDs bound to it
                var variantCustomers by remember { mutableStateOf<Map<Int, Set<Int>>>(emptyMap()) }
                // Optional cache of all customers for name resolution when adminToken present
                var allCustomersCache by remember { mutableStateOf<List<kz.ruccola.food.api.CustomerDto>?>(null) }
                var customersLoading by remember { mutableStateOf(false) }

                // If we returned from the Variant Editor with a pending save, apply it now
                LaunchedEffect(variantSavePending) {
                    val pending = variantSavePending ?: return@LaunchedEffect
                    variants = if (pending.isCreate) {
                        variants + pending.item
                    } else {
                        variants.map { if (it.id == pending.item.id) pending.item else it }
                    }
                    variantCustomers = variantCustomers.toMutableMap().also { map ->
                        map[pending.item.id] = pending.selectedCustomerIds
                    }
                    variantSavePending = null
                }

                LaunchedEffect(dishState?.id) {
                    val id = dishState?.id ?: return@LaunchedEffect
                    val res = dishRepo.getVariants(id)
                    res.fold(onSuccess = { list ->
                        variants = list
                        variantsLoaded = true
                        // After variants loaded, fetch variant->customers and optionally all customers for display
                        customersLoading = true
                        variantCustomers = emptyMap()
                        allCustomersCache = null
                        try {
                            // Load all customers at once if adminToken available (to resolve names)
                            if (adminToken != null) {
                                val customerRepo = kz.ruccola.food.repository.CustomerRepository()
                                val listRes = customerRepo.getCustomers(adminToken)
                                listRes.fold(
                                    onSuccess = { allCustomersCache = it },
                                    onFailure = { e2 -> error = e2.message },
                                )
                            }
                            // Load customers for each variant sequentially to keep it simple
                            val map = mutableMapOf<Int, Set<Int>>()
                            for (v in list) {
                                val cur = dishRepo.getVariantCustomers(dishState!!.id, v.id)
                                cur.fold(
                                    onSuccess = { ids -> map[v.id] = ids.toSet() },
                                    onFailure = { e2 -> error = e2.message },
                                )
                            }
                            variantCustomers = map
                        } finally {
                            customersLoading = false
                        }
                    }, onFailure = { e ->
                        error = e.message
                        variantsLoaded = true
                    })
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Variants", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(enabled = !busy && dishState != null, onClick = {
                        variantEditorExisting = null
                        variantEditorInitialCustomerIds = null
                        variantEditorOpen = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add variant")
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (!variantsLoaded) {
                    LinearProgressIndicator()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        variants.forEach { v ->
                            key(v.id) {
                                val onDelete: () -> Unit = {
                                    if (!busy) {
                                        busy = true
                                        val idToDelete = v.id
                                        scope.launch {
                                            val res = dishRepo.deleteVariant(dishState!!.id, idToDelete)
                                            res.fold(onSuccess = {
                                                variants = variants.filter { it.id != idToDelete }
                                                busy = false
                                            }, onFailure = { e ->
                                                error = e.message
                                                busy = false
                                            })
                                        }
                                    }
                                }
                                SwipeToRemove(
                                    Icons.Default.Delete,
                                    Strings.delete,
                                    onDelete,
                                    CardDefaults.outlinedShape,
                                ) {
                                    OutlinedCard {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                                .let { base ->
                                                    if (!busy) {
                                                        base.clickable {
                                                            variantEditorExisting = v
                                                            variantEditorInitialCustomerIds =
                                                                variantCustomers[v.id] ?: emptySet()
                                                            variantEditorOpen = true
                                                        }
                                                    } else {
                                                        base
                                                    }
                                                },
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(
                                                    v.description,
                                                    modifier = Modifier.weight(1f),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            // Sublist of bound customers under the variant description
                                            val ids = variantCustomers[v.id]
                                            when {
                                                customersLoading && ids == null -> {
                                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                                }

                                                ids == null -> {
                                                    Text(
                                                        "Customers: —",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }

                                                ids.isEmpty() -> {
                                                    Text(
                                                        "No customers bound",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }

                                                else -> {
                                                    val all = allCustomersCache
                                                    val names = ids.take(6).map { cid ->
                                                        if (all != null) {
                                                            val c = all.firstOrNull { it.id == cid }
                                                            if (c != null) "${c.firstName} ${c.lastName}" else "ID $cid"
                                                        } else {
                                                            "ID $cid"
                                                        }
                                                    }
                                                    val more = if (ids.size > 6) ids.size - 6 else 0
                                                    val line =
                                                        if (more >
                                                            0
                                                        ) {
                                                            names.joinToString(", ") + " +$more more"
                                                        } else {
                                                            names.joinToString(
                                                                ", ",
                                                            )
                                                        }
                                                    Text(
                                                        "Customers: $line",
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
            if (busy) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
