package kz.ruccola.food.admin.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.web.common.dishImageUrl
import kz.ruccola.food.web.common.ui.AsyncImage
import kz.ruccola.food.web.common.ui.SingleLineText
import kotlin.math.roundToInt

@Composable
fun DishScreen() {
    var dishes by remember { mutableStateOf<List<DishDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var editorVisible by remember { mutableStateOf(false) }
    var editingDish by remember { mutableStateOf<DishDto?>(null) }

    val scope = rememberCoroutineScope()
    val dishApi = remember { DishApi() }

    fun loadDishes() {
        scope.launch {
            isLoading = true
            error = null
            try {
                dishes = dishApi.getAllDishes().filter { !it.archived }
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки блюд"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadDishes() }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            if (!editorVisible) {
                CenterAlignedTopAppBar(
                    title = { Text(Strings.tabDishes) },
                    actions = {
                        IconButton(onClick = { loadDishes() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (!editorVisible) {
                FloatingActionButton(
                    onClick = {
                        editingDish = null
                        editorVisible = true
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading dishes...")
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Ошибка: $error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { loadDishes() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(Strings.retry)
                        }
                    }
                }

                dishes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Блюда не найдены", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Нажмите + чтобы добавить", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            editingDish = null
                            editorVisible = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить блюдо")
                        }
                    }
                }

                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(dishes) { dish ->
                            DishListItem(
                                dish = dish,
                                onClick = {
                                    editingDish = dish
                                    editorVisible = true
                                },
                                onArchive = {
                                    scope.launch {
                                        try {
                                            dishApi.archiveDish(dish.id)
                                            loadDishes()
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (editorVisible) {
        DishEditorScreen(
            initialDish = editingDish,
            onClose = {
                editorVisible = false
                editingDish = null
                loadDishes()
            },
        )
    }
}

@Composable
fun DishListItem(
    dish: DishDto,
    onClick: () -> Unit,
    onArchive: () -> Unit,
) {
    var showArchiveDialog by remember { mutableStateOf(false) }
    val imageUrl = dish.images.firstOrNull()?.url
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 80.dp.toPx() }
    val swipeReady = kotlin.math.abs(dragOffset) >= swipeThresholdPx
    val swipeIconTint = if (swipeReady) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Archive, contentDescription = null, tint = swipeIconTint)
                Text(
                    text = Strings.archive,
                    style = MaterialTheme.typography.labelSmall,
                    color = swipeIconTint,
                )
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Archive, contentDescription = null, tint = swipeIconTint)
                Text(
                    text = Strings.archive,
                    style = MaterialTheme.typography.labelSmall,
                    color = swipeIconTint,
                )
            }
        }
        ListItem(
            leadingContent = {
                if (imageUrl != null) {
                    AsyncImage(
                        model = dishImageUrl(imageUrl),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
                    )
                }
            },
            headlineContent = { SingleLineText(dish.name) },
            supportingContent = { SingleLineText(dish.description) },
            modifier = Modifier.fillMaxWidth()
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
                .pointerInput(dish.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            if (kotlin.math.abs(dragOffset) >= swipeThresholdPx) {
                                showArchiveDialog = true
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                    )
                }
                .clickable(onClick = onClick),
        )
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Архивировать блюдо?") },
            text = { Text("Блюдо \"${dish.name}\" будет архивировано и скрыто из списка.") },
            confirmButton = {
                TextButton(onClick = {
                    showArchiveDialog = false
                    onArchive()
                }) {
                    Text("Архивировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text(Strings.cancel)
                }
            },
        )
    }
}
