package kz.ruccola.food.feature.dish

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.archive
import food.composeappadmin.generated.resources.error
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.no_items
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.tab_dishes
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.dishImageUrl
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.PullToRefresh
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeToRemove
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DishScreen() {
    val viewModel: DishViewModel = viewModel(factory = DishViewModel.factory())
    val dishes = viewModel.dishes.collectAsLazyPagingItems()

    var editorVisible by remember { mutableStateOf(false) }
    val selectedDish by viewModel.uiState.collectAsState()

    val ptrState = rememberPullToRefreshState()
    val thresholdPx = with(LocalDensity.current) { 100.dp.toPx() }

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
                        viewModel.clearSelectedDish()
                        editorVisible = true
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.add))
                }
            }
        },
    ) { paddingValues ->
        PullToRefresh(
            isRefreshing = dishes.loadState.refresh is LoadState.Loading,
            onRefresh = { dishes.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            state = ptrState,
        ) {
            when {
                dishes.loadState.refresh is LoadState.Error -> {
                    val error = (dishes.loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(Res.string.error_prefix, error.message ?: stringResource(Res.string.error)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                dishes.refresh()
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

                dishes.itemCount == 0 && dishes.loadState.refresh !is LoadState.Loading -> {
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
                        items(
                            count = dishes.itemCount,
                            key = dishes.itemKey { it.id },
                        ) { index ->
                            val dish = dishes[index]
                            if (dish != null) {
                                DishListItem(
                                    dish = dish,
                                    onEdit = {
                                        viewModel.getDishById(dish.id)
                                        editorVisible = true
                                    },
                                    onArchive = {
                                        viewModel.archiveDish(dish.id)
                                        dishes.refresh()
                                    },
                                )
                            }
                        }

                        if (dishes.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (editorVisible) {
            DishEditorScreen(
                initialDish = selectedDish.selectedDish,
                onClose = {
                    editorVisible = false
                    dishes.refresh()
                },
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
    val displayName = dish.name
    val displayDescription = dish.description

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
            headlineContent = { SingleLineText(displayName) },
            supportingContent = { SingleLineText(displayDescription) },
            modifier = Modifier.clickable(onClick = onEdit),
        )
    }
}
