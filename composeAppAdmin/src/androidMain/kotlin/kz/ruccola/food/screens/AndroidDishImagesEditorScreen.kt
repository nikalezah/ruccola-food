package kz.ruccola.food.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishImageDto
import kz.ruccola.food.repository.DishRepository
import kz.ruccola.food.repository.FileRepository
import kz.ruccola.food.ui.SwipeToDeleteItem
import kz.ruccola.food.ui.dishImageUrl
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidDishImagesEditorScreen(
    dish: DishDto,
    onBack: () -> Unit,
    onDishUpdated: (DishDto) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dishRepo = remember { DishRepository() }
    val fileRepo = remember { FileRepository() }

    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val images = remember(dish.id) { mutableStateListOf(*dish.images.toTypedArray()) }

    // Back handler inside this screen
    BackHandler(enabled = true) {
        if (!busy) onBack()
    }

    // Image picker for adding new image
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            busy = true
            error = null
            scope.launch {
                val uploadRes = fileRepo.upload(context, uri)
                uploadRes.fold(onSuccess = { uploaded ->
                    // Append locally and persist
                    val newList = images.toList() + DishImageDto(id = -uploaded.id, url = "", fileId = uploaded.id)
                    persistImages(
                        dishId = dish.id,
                        list = newList,
                        repo = dishRepo,
                        onSuccess = { updated ->
                            images.clear()
                            images.addAll(updated.images)
                            onDishUpdated(updated)
                            busy = false
                            Toast.makeText(context, "Image added", Toast.LENGTH_SHORT).show()
                        },
                        onError = { e ->
                            error = e
                            busy = false
                        },
                    )
                }, onFailure = { e ->
                    error = e.message
                    busy = false
                })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit images") },
                navigationIcon = {
                    IconButton(onClick = { if (!busy) onBack() }, enabled = !busy) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { pickImageLauncher.launch("image/*") }, enabled = !busy) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (images.isEmpty()) {
                Text("No images yet")
            } else {
                // Simple reorderable list with swipe-to-delete
                val density = LocalDensity.current
                val itemHeightPx = with(density) { 72.dp.toPx() }

                images.forEachIndexed { index, img ->
                    key(img.id to index) {
                        SwipeToDeleteItem(onDelete = {
                            if (busy) return@SwipeToDeleteItem
                            busy = true
                            scope.launch {
                                val remaining = images.filterIndexed { i, _ -> i != index }
                                persistImages(
                                    dishId = dish.id,
                                    list = remaining,
                                    repo = dishRepo,
                                    onSuccess = { updated ->
                                        images.clear()
                                        images.addAll(updated.images)
                                        onDishUpdated(updated)
                                        busy = false
                                        // Also try to delete the file if it's ours
                                        val fid = img.fileId
                                        if (fid != null) scope.launch { fileRepo.delete(fid) }
                                    },
                                    onError = { e ->
                                        error = e
                                        busy = false
                                    },
                                )
                            }
                        }) {
                            var localOffsetY by remember { mutableFloatStateOf(0f) }
                            var currentIndex by remember { mutableIntStateOf(index) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .offset { IntOffset(0, localOffsetY.roundToInt()) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AsyncImage(
                                        model = dishImageUrl(img.url),
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "Image #${index + 1}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f),
                                    )
                                    // Use a drag grip; only this captures drag gestures, leaving swipe free on the card
                                    val thresholdPx = with(LocalDensity.current) { 80.dp.toPx() }
                                    Icon(
                                        imageVector = Icons.Filled.DragHandle,
                                        contentDescription = "Reorder",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .pointerInput(images.size) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        localOffsetY = 0f
                                                        currentIndex = index
                                                    },
                                                    onDragCancel = { localOffsetY = 0f },
                                                    onDragEnd = {
                                                        if (currentIndex != index) {
                                                            busy = true
                                                            scope.launch {
                                                                persistImages(
                                                                    dishId = dish.id,
                                                                    list = images,
                                                                    repo = dishRepo,
                                                                    onSuccess = { updated ->
                                                                        images.clear()
                                                                        images.addAll(updated.images)
                                                                        onDishUpdated(updated)
                                                                        busy = false
                                                                    },
                                                                    onError = { e ->
                                                                        error = e
                                                                        busy = false
                                                                    },
                                                                )
                                                            }
                                                        }
                                                        localOffsetY = 0f
                                                    },
                                                ) { _, drag ->
                                                    localOffsetY += drag.y
                                                    while (localOffsetY > thresholdPx &&
                                                        currentIndex < images.lastIndex
                                                    ) {
                                                        localOffsetY -= thresholdPx
                                                        val from = currentIndex
                                                        val to = currentIndex + 1
                                                        images.swap(from, to)
                                                        currentIndex = to
                                                    }
                                                    while (localOffsetY < -thresholdPx && currentIndex > 0) {
                                                        localOffsetY += thresholdPx
                                                        val from = currentIndex
                                                        val to = currentIndex - 1
                                                        images.swap(from, to)
                                                        currentIndex = to
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

            if (busy) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun MutableList<DishImageDto>.swap(
    i: Int,
    j: Int,
) {
    if (i == j) return
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

private fun persistImages(
    dishId: Int,
    list: List<DishImageDto>,
    repo: DishRepository,
    onSuccess: (DishDto) -> Unit,
    onError: (String?) -> Unit,
) {
    // Launch in coroutine where called
    GlobalScope.launch {
        try {
            val updated = repo.updateDishImages(dishId, list.map { it.fileId })
            updated.fold(onSuccess = { onSuccess(it) }, onFailure = { onError(it.message) })
        } catch (e: Exception) {
            onError(e.message)
        }
    }
}
