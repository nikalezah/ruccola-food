package kz.ruccola.food.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeBackground
import kz.ruccola.food.ui.dishImageUrl
import kz.ruccola.food.viewmodel.DishViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidDishScreen(adminToken: String? = null) {
    val viewModel: DishViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Show toast for errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    var editorVisible by remember { mutableStateOf(false) }
    var editorDish by remember { mutableStateOf<DishDto?>(null) }

    val ptrState = rememberPullToRefreshState()
    val threshold = 100.dp
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }

    Scaffold(
        topBar = {
            if (!editorVisible) {
                CenterAlignedTopAppBar(
                    title = { Text("Блюда") },
                )
            }
        },
        floatingActionButton = {
            if (!editorVisible) {
                FloatingActionButton(
                    onClick = {
                        editorDish = null
                        editorVisible = true
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
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
                        Text("Loading dishes...")
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Error: ${uiState.error}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.loadDishes()
                                Toast.makeText(context, "Retrying...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                uiState.dishes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "No dishes found",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add one by clicking the + button",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                editorDish = null
                                editorVisible = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Dish",
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Dish")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            translationY = ptrState.distanceFraction * thresholdPx
                        },
                    ) {
                        items(uiState.dishes) { d ->
                            val imageUrl = d.images.firstOrNull()?.url
                            val dismissState = rememberSwipeToDismissBoxState()

                            LaunchedEffect(dismissState.currentValue) {
                                if (
                                    dismissState.currentValue == SwipeToDismissBoxValue.EndToStart ||
                                    dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd
                                ) {
                                    viewModel.archiveDish(d.id)
                                    Toast.makeText(context, "Archiving: ${d.name}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            modifier = Modifier.matchParentSize().padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            SwipeBackground(Icons.Default.Archive, "Archive")
                                            SwipeBackground(Icons.Default.Archive, "Archive")
                                        }
                                    }
                                },
                            ) {
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
                                    headlineContent = { SingleLineText(d.name) },
                                    supportingContent = { SingleLineText(d.description) },
                                    modifier = Modifier.clickable {
                                        editorDish = d
                                        editorVisible = true
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Navigate to the editor screen
        if (editorVisible) {
            AndroidDishEditorScreen(
                initialDish = editorDish,
                onClose = {
                    editorVisible = false
                    editorDish = null
                    viewModel.loadDishes()
                },
                adminToken = adminToken,
            )
        }
    }
}
