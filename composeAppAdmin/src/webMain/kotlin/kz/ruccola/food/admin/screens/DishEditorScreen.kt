package kz.ruccola.food.admin.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.web.common.ui.SquareImagesCarousel200
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishEditorScreen(
    initialDish: DishDto?,
    onClose: () -> Unit,
) {
    val dishApi = remember { DishApi() }
    val customerApi = remember { CustomerApi() }
    val scope = rememberCoroutineScope()

    var dishState by remember { mutableStateOf(initialDish) }
    var name by remember { mutableStateOf(initialDish?.name ?: "") }
    var description by remember { mutableStateOf(initialDish?.description ?: "") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var variants by remember { mutableStateOf<List<DishVariantDto>>(emptyList()) }
    var variantsLoaded by remember { mutableStateOf(false) }

    var variantCustomers by remember { mutableStateOf<Map<Int, Set<Int>>>(emptyMap()) }
    var allCustomers by remember { mutableStateOf<List<CustomerDto>?>(null) }
    var customersLoading by remember { mutableStateOf(false) }

    var variantEditorVisible by remember { mutableStateOf(false) }
    var editingVariant by remember { mutableStateOf<DishVariantDto?>(null) }
    var variantToDelete by remember { mutableStateOf<DishVariantDto?>(null) }
    var imageEditorVisible by remember { mutableStateOf(false) }
    val adminToken = remember { kotlinx.browser.window.localStorage.getItem("admin.token") ?: "" }

    fun loadVariants() {
        val id = dishState?.id ?: return
        scope.launch {
            try {
                val list = dishApi.getVariants(id)
                variants = list
                variantsLoaded = true

                customersLoading = true
                try {
                    val map = mutableMapOf<Int, Set<Int>>()
                    for (v in list) {
                        try {
                            val ids = dishApi.getVariantCustomers(id, v.id)
                            map[v.id] = ids.toSet()
                        } catch (e: Exception) {
                            // ignore for single variant
                        }
                    }
                    variantCustomers = map
                } finally {
                    customersLoading = false
                }
            } catch (e: Exception) {
                error = e.message
                variantsLoaded = true
            }
        }
    }

    fun deleteVariant(v: DishVariantDto) {
        val id = dishState?.id ?: return
        scope.launch {
            busy = true
            try {
                dishApi.deleteVariant(id, v.id)
                loadVariants()
            } catch (e: Exception) {
                error = e.message
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(dishState?.id) {
        loadVariants()
    }

    LaunchedEffect(adminToken) {
        if (adminToken.isNotBlank()) {
            try {
                allCustomers = customerApi.getAll(adminToken)
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    fun saveDish() {
        scope.launch {
            busy = true
            error = null
            try {
                if (dishState == null) {
                    val created =
                        dishApi.createDish(DishCreateDto(name = name.trim(), description = description.trim()))
                    dishState = created
                } else {
                    val updated = dishApi.updateDish(
                        dishState!!.id,
                        DishUpdateDto(name = name.trim(), description = description.trim()),
                    )
                    dishState = updated
                }
            } catch (e: Exception) {
                error = e.message ?: "Ошибка сохранения"
            } finally {
                busy = false
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
                        FilledIconButton(onClick = { saveDish() }, enabled = name.isNotBlank() && !busy) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
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

            if (dishState == null) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.name) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(Strings.description) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                Spacer(Modifier.height(12.dp))
            } else {
                var showEditName by remember { mutableStateOf(false) }
                var showEditDescription by remember { mutableStateOf(false) }

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
                                scope.launch {
                                    busy = true
                                    try {
                                        val updated =
                                            dishApi.updateDish(dishState!!.id, DishUpdateDto(name = temp.trim()))
                                        dishState = updated
                                        name = updated.name
                                    } catch (e: Exception) {
                                        error = e.message
                                    } finally {
                                        busy = false
                                    }
                                }
                            }) { Text(Strings.save) }
                        },
                        dismissButton = { TextButton(onClick = { showEditName = false }) { Text(Strings.cancel) } },
                        title = { Text("Изменить название") },
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
                                scope.launch {
                                    busy = true
                                    try {
                                        val updated =
                                            dishApi.updateDish(dishState!!.id, DishUpdateDto(description = temp.trim()))
                                        dishState = updated
                                        description = updated.description
                                    } catch (e: Exception) {
                                        error = e.message
                                    } finally {
                                        busy = false
                                    }
                                }
                            }) { Text(Strings.save) }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showEditDescription = false
                            }) { Text(Strings.cancel) }
                        },
                        title = { Text("Изменить описание") },
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
                    Text("Изображения", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { imageEditorVisible = true }, enabled = !busy) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit images")
                    }
                }
                Spacer(Modifier.height(8.dp))
                SquareImagesCarousel200(imageUrls = dishState?.images?.map { it.url } ?: emptyList())

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Варианты", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(enabled = !busy, onClick = {
                        editingVariant = null
                        variantEditorVisible = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add variant")
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (!variantsLoaded) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    variants.forEach { v ->
                        key(v.id) {
                            var dragOffset by remember { mutableFloatStateOf(0f) }
                            val density = LocalDensity.current
                            val swipeThresholdPx = with(density) { 80.dp.toPx() }
                            val swipeReady = kotlin.math.abs(dragOffset) >= swipeThresholdPx
                            val swipeIconTint = if (swipeReady) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            LaunchedEffect(busy) {
                                if (busy) {
                                    dragOffset = 0f
                                }
                            }

                            Box(Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                        .heightIn(min = 72.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = swipeIconTint)
                                        Text(
                                            text = Strings.delete,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = swipeIconTint,
                                        )
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = swipeIconTint)
                                        Text(
                                            text = Strings.delete,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = swipeIconTint,
                                        )
                                    }
                                }
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .offset { IntOffset(dragOffset.roundToInt(), 0) }
                                        .then(
                                            if (!busy) {
                                                Modifier.pointerInput(v.id) {
                                                    detectHorizontalDragGestures(
                                                        onHorizontalDrag = { change, dragAmount ->
                                                            change.consume()
                                                            dragOffset += dragAmount
                                                        },
                                                        onDragEnd = {
                                                            if (kotlin.math.abs(dragOffset) >= swipeThresholdPx) {
                                                                variantToDelete = v
                                                            }
                                                            dragOffset = 0f
                                                        },
                                                        onDragCancel = { dragOffset = 0f },
                                                    )
                                                }
                                            } else {
                                                Modifier
                                            },
                                        ),
                                    onClick = {
                                        editingVariant = v
                                        variantEditorVisible = true
                                    },
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.padding(12.dp).weight(1f)) {
                                            Text(v.description, style = MaterialTheme.typography.bodyLarge)
                                            Spacer(Modifier.height(4.dp))

                                            val ids = variantCustomers[v.id]
                                            when {
                                                customersLoading && ids == null -> {
                                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                                }

                                                ids.isNullOrEmpty() -> {
                                                    Text(
                                                        Strings.noCustomersBound,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }

                                                else -> {
                                                    val names = ids.map { cid ->
                                                        val c = allCustomers?.find { it.id == cid }
                                                        if (c != null) "${c.firstName} ${c.lastName}" else "ID $cid"
                                                    }
                                                    val line = names.joinToString(", ")
                                                    Text(
                                                        Strings.labelCustomers.replace("%s", line),
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
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (variantEditorVisible && dishState != null) {
        DishVariantEditorScreen(
            dishId = dishState!!.id,
            dishName = dishState!!.name,
            token = adminToken,
            existing = editingVariant,
            initialCustomerIds = editingVariant?.let { variantCustomers[it.id] },
            onClose = { variantEditorVisible = false },
            onSaved = {
                variantEditorVisible = false
                loadVariants()
            },
        )
    }

    if (imageEditorVisible && dishState != null) {
        DishImagesEditorScreen(
            dish = dishState!!,
            onClose = { imageEditorVisible = false },
            onSaved = { updated -> dishState = updated },
        )
    }

    if (variantToDelete != null) {
        AlertDialog(
            onDismissRequest = { variantToDelete = null },
            title = { Text("Удалить вариант?") },
            text = { Text("Вы уверены, что хотите удалить вариант \"${variantToDelete!!.description}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    val v = variantToDelete!!
                    variantToDelete = null
                    deleteVariant(v)
                }) {
                    Text(Strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { variantToDelete = null }) {
                    Text(Strings.cancel)
                }
            },
        )
    }
}
