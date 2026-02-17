package kz.ruccola.food.admin.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.FileApi
import kz.ruccola.food.web.common.dishImageUrl
import kz.ruccola.food.web.common.ui.AsyncImage
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.js.ExperimentalWasmJsInterop

// todo: move somewhere
private data class DishImageItem(
    val fileId: Int,
    val url: String,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalWasmJsInterop::class)
@Composable
fun DishImagesEditorScreen(
    dish: DishDto,
    onClose: () -> Unit,
    onSaved: (DishDto) -> Unit,
) {
    val dishApi = remember { DishApi() }
    val fileApi = remember { FileApi() }
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val workingList = remember { mutableStateListOf<DishImageItem>() }
    val initialIds = remember(dish.id, dish.images) { dish.images.map { it.fileId }.toSet() }
    val originalPositions = remember(dish.id, dish.images) {
        dish.images.mapIndexed { index, image -> image.fileId to (index + 1) }.toMap()
    }

    LaunchedEffect(dish.id, dish.images) {
        workingList.clear()
        workingList.addAll(dish.images.map { DishImageItem(it.fileId, it.url) })
    }

    fun uploadImage() {
        if (busy) return
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val file = input.files?.item(0)!! // ?: return@onchange
            scope.launch {
                busy = true
                try {
                    val reader = FileReader()
                    reader.onload = { _ ->
                        val result = reader.result as ArrayBuffer
                        val array = Int8Array(result)
                        val bytes = ByteArray(array.length) { i -> array[i] }

                        scope.launch {
                            try {
                                val uploaded = fileApi.upload(file.name, file.type, bytes)
                                workingList.add(DishImageItem(fileId = uploaded.id, url = uploaded.url))
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                busy = false
                            }
                        }
                    }
                    reader.readAsArrayBuffer(file)
                } catch (e: Exception) {
                    error = e.message
                    busy = false
                }
            }
        }
        input.click()
    }

    fun saveChanges() {
        if (busy) return
        scope.launch {
            busy = true
            error = null
            val currentIds = workingList.map { it.fileId }
            try {
                val updated = dishApi.updateDish(dish.id, DishUpdateDto(imageFileIds = currentIds))
                val removedIds = initialIds - currentIds.toSet()
                for (id in removedIds) {
                    runCatching { fileApi.delete(id) }
                }
                onSaved(updated)
                onClose()
            } catch (e: Exception) {
                error = e.message
            } finally {
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit images") },
                navigationIcon = {
                    IconButton(onClick = { if (!busy) onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledIconButton(onClick = { saveChanges() }, enabled = !busy) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { uploadImage() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            if (busy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            if (workingList.isEmpty()) {
                Text(Strings.noItems, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(workingList, key = { _, item -> item.fileId }) { index, item ->
                        val previousPosition = originalPositions[item.fileId] ?: (index + 1)
                        var dragOffset by remember { mutableFloatStateOf(0f) }
                        val density = LocalDensity.current
                        val swipeThresholdPx = with(density) { 80.dp.toPx() }
                        val swipeReady = kotlin.math.abs(dragOffset) >= swipeThresholdPx
                        val swipeIconTint =
                            if (swipeReady) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                        LaunchedEffect(busy) {
                            if (busy) {
                                dragOffset = 0f
                            }
                        }

                        Box(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .heightIn(min = 80.dp),
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
                                    .graphicsLayer { translationX = dragOffset }
                                    .then(
                                        if (!busy) {
                                            Modifier.pointerInput(item.fileId) {
                                                detectHorizontalDragGestures(
                                                    onHorizontalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffset += dragAmount
                                                    },
                                                    onDragEnd = {
                                                        if (kotlin.math.abs(dragOffset) >= swipeThresholdPx) {
                                                            workingList.remove(item)
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
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AsyncImage(
                                        model = dishImageUrl(item.url),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.extraSmall),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$previousPosition", style = MaterialTheme.typography.bodyLarge)
                                        if (previousPosition != index + 1) {
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "${index + 1}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = {
                                            val idx = workingList.indexOf(item)
                                            if (idx > 0) {
                                                workingList.removeAt(idx)
                                                workingList.add(idx - 1, item)
                                            }
                                        },
                                        enabled = !busy && index > 0,
                                    ) {
                                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                                    }
                                    IconButton(
                                        onClick = {
                                            val idx = workingList.indexOf(item)
                                            if (idx < workingList.size - 1) {
                                                workingList.removeAt(idx)
                                                workingList.add(idx + 1, item)
                                            }
                                        },
                                        enabled = !busy && index < workingList.size - 1,
                                    ) {
                                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
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
