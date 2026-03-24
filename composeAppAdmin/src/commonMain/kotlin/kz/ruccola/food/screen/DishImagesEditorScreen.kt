package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.close
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.images
import food.composeappadmin.generated.resources.no_items
import food.composeappadmin.generated.resources.save
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.dishImageUrl
import kz.ruccola.food.provideImagePicker
import kz.ruccola.food.ui.ApplyIconButton
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SwipeToRemove
import kz.ruccola.food.viewmodel.DishImagesViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishImagesEditorScreen(
    dish: DishDto,
    onClose: () -> Unit,
    onSaved: (DishDto) -> Unit,
) {
    val viewModel = remember(dish.id) { DishImagesViewModel(dish) }
    val uiState by viewModel.uiState.collectAsState()
    val onPickImage = provideImagePicker()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved(uiState.dish)
            onClose()
        }
    }

    val originalPositions = remember(dish.id, dish.images) {
        dish.images.mapIndexed { index, image -> image.fileId to (index + 1) }.toMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.images)) },
                navigationIcon = {
                    IconButton(onClick = { if (!uiState.isBusy) onClose() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(Res.string.close))
                    }
                },
                actions = {
                    ApplyIconButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.hasChanges && !uiState.isBusy,
                        contentDescription = stringResource(Res.string.save),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (!uiState.isBusy) onPickImage(viewModel) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.add))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            if (uiState.isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            if (uiState.workingList.isEmpty()) {
                Text(stringResource(Res.string.no_items), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(uiState.workingList, key = { _, item -> item.fileId }) { index, item ->
                        val previousPosition = originalPositions[item.fileId] ?: (index + 1)
                        SwipeToRemove(
                            Icons.Filled.Delete,
                            stringResource(Res.string.delete),
                            { viewModel.removeImage(item) },
                            CardDefaults.outlinedShape,
                            enabled = !uiState.isBusy,
                        ) {
                            OutlinedCard {
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
                                                Icons.Filled.ArrowForward,
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
                                        onClick = { viewModel.moveUp(index) },
                                        enabled = !uiState.isBusy && index > 0,
                                    ) {
                                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveDown(index) },
                                        enabled = !uiState.isBusy && index < uiState.workingList.size - 1,
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
