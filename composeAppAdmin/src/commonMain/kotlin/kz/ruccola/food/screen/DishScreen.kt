package kz.ruccola.food.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.archive
import food.composeappadmin.generated.resources.error
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.loading
import food.composeappadmin.generated.resources.no_items
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.tab_dishes
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeToRemove
import kz.ruccola.food.ui.dishImageUrl
import kz.ruccola.food.viewmodel.DishViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DishScreen(token: String? = null) {
    val viewModel: DishViewModel = viewModel(factory = DishViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    var editorVisible by remember { mutableStateOf(false) }
    var editingDish by remember { mutableStateOf<DishDto?>(null) }

    val ptrState = rememberPullToRefreshState()
    val threshold = 100.dp
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }

    Scaffold(
        topBar = {
            if (!editorVisible) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.tab_dishes)) },
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
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.add))
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadDishes() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            state = ptrState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = ptrState,
                    isRefreshing = uiState.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(Res.string.loading))
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(Res.string.error_prefix, uiState.error ?: stringResource(Res.string.error)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.loadDishes()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(Res.string.retry),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }

                uiState.dishes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.no_items))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            translationY = ptrState.distanceFraction * thresholdPx
                        },
                    ) {
                        items(uiState.dishes, key = { it.id }) { dish ->
                            DishListItem(
                                dish = dish,
                                onEdit = {
                                    editingDish = dish
                                    editorVisible = true
                                },
                                onArchive = { viewModel.archiveDish(dish.id) },
                            )
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
                    viewModel.loadDishes()
                },
                token,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DishListItem(
    dish: DishDto,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    val imageUrl = dish.images.firstOrNull()?.url

    SwipeToRemove(Icons.Filled.Archive, stringResource(Res.string.archive), onArchive) {
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
            modifier = Modifier.clickable(onClick = onEdit),
        )
    }
}
