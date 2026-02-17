package kz.ruccola.food.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.repository.MealPlanDayRepository
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeBackground
import kz.ruccola.food.viewmodel.MealPlanDayViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidMealPlanDayScreen(onHistoryClick: () -> Unit) {
    val vm: MealPlanDayViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val ctx = LocalContext.current

    val ptrState = rememberPullToRefreshState()
    val threshold = 100.dp
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val targetY = if (state.isLoading) 0f else (ptrState.distanceFraction * thresholdPx)
    val animatedTranslationY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = spring(),
        label = "bounce_animation",
    )
    LaunchedEffect(ptrState.distanceFraction) {
        if (ptrState.distanceFraction >= 1f && !state.isLoading) {
            vm.getAll()
        }
    }

    var editorVisible by remember { mutableStateOf(false) }
    var editorTarget by remember { mutableStateOf<MealPlanDayDto?>(null) }
    var nextSerial by remember { mutableIntStateOf(1) }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    var reorderMode by remember { mutableStateOf(false) }

    // Local working copy for reordering
    val workingList = remember { mutableStateListOf<MealPlanDayDto>() }
    LaunchedEffect(state.items) {
        // Always reflect updated items (including refreshed serials) in the working list
        workingList.clear()
        workingList.addAll(state.items)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Расписание") },
                navigationIcon = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                },
                actions = {
                    FilledIconToggleButton(
                        checked = reorderMode,
                        onCheckedChange = { checked ->
                            if (reorderMode) {
                                // Exiting reorder mode without a drag: persist current order
                                vm.reorder(workingList.map { it.id })
                            }
                            reorderMode = checked
                        },
                        modifier = Modifier.size(
                            IconButtonDefaults.smallContainerSize(
                                widthOption = IconButtonDefaults.IconButtonWidthOption.Wide,
                            ),
                        ),
                    ) {
                        if (reorderMode) {
                            Icon(Icons.Filled.Check, contentDescription = "Done")
                        } else {
                            Icon(Icons.Filled.SwapVert, contentDescription = "Reorder")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nextSerial = (state.items.maxOfOrNull { it.serial } ?: 0) + 1
                    editorTarget = null
                    editorVisible = true
                },
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { vm.getAll() },
            modifier = Modifier.fillMaxSize().padding(padding),
            state = ptrState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = ptrState,
                    isRefreshing = state.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            val density = LocalDensity.current
            val itemHeightPx = with(density) { 72.dp.toPx() }
            var draggingIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp)
                    .graphicsLayer { translationY = animatedTranslationY },
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(workingList, key = { _, it -> it.id }) { index, item ->
                    var offsetY by remember(item.id) { mutableFloatStateOf(0f) }
                    val isDragging = draggingIndex == index

                    val onMove = { from: Int, to: Int ->
                        if (from in workingList.indices && to in workingList.indices) {
                            val item = workingList.removeAt(from)
                            workingList.add(to, item)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f).scale(if (isDragging) 1.02f else 1f)) {
                            AndroidMealPlanDayItem(
                                item = item,
                                onClick = {
                                    editorTarget = item
                                    editorVisible = true
                                },
                                onSwipeDelete = { vm.delete(item.id) },
                                onMakeCurrent = { vm.setCurrent(item.id) },
                            )
                        }
                        if (reorderMode) {
                            Spacer(Modifier.width(8.dp))
                            // Drag handle on the right
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(72.dp)
                                    .pointerInput(workingList.size) {
                                        detectDragGestures(
                                            onDragStart = {
                                                draggingIndex = index
                                                offsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                offsetY += dragAmount.y
                                                val moveBy = (offsetY / itemHeightPx).toInt()
                                                if (moveBy != 0 && draggingIndex != null) {
                                                    val currentIdx = draggingIndex!!
                                                    val targetIdx =
                                                        (currentIdx + moveBy).coerceIn(0, workingList.lastIndex)
                                                    if (targetIdx != currentIdx) {
                                                        if (targetIdx > currentIdx) {
                                                            for (i in currentIdx until targetIdx) onMove(i, i + 1)
                                                        } else {
                                                            for (i in currentIdx downTo targetIdx + 1) onMove(i, i - 1)
                                                        }
                                                        draggingIndex = targetIdx
                                                        offsetY -= moveBy * itemHeightPx
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                offsetY = 0f
                                                draggingIndex = null
                                            },
                                            onDragCancel = {
                                                offsetY = 0f
                                                draggingIndex = null
                                            },
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.DragIndicator, contentDescription = "Drag")
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorVisible) {
        AndroidMealPlanDayEditorScreen(
            initialItem = editorTarget,
            nextSerial = nextSerial,
            onClose = {
                editorVisible = false
                editorTarget = null
                vm.getAll()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidMealPlanDayItem(
    item: MealPlanDayDto,
    onClick: () -> Unit,
    onSwipeDelete: () -> Unit,
    onMakeCurrent: () -> Unit,
) {
    val repository = remember { MealPlanDayRepository() }
    var dishes by remember { mutableStateOf<List<DishWithMealDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.id) {
        isLoading = true
        error = null
        val result = repository.getDishes(item.id)
        result.onSuccess { list -> dishes = list }.onFailure { e -> error = e.message }
        isLoading = false
    }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (
            dismissState.currentValue == SwipeToDismissBoxValue.EndToStart ||
            dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd
        ) {
            onSwipeDelete()
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
                    SwipeBackground(Icons.Default.Delete, "Delete")
                    SwipeBackground(Icons.Default.Delete, "Delete")
                }
            }
        },
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(PaddingValues(0.dp, 8.dp, 8.dp, 8.dp))
                    .height(142.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp, 0.dp).width(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SingleLineText("#${item.serial}", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onMakeCurrent, enabled = !item.current) {
                        Icon(Icons.Default.Today, contentDescription = "Current")
                    }
                }
                VerticalDivider()
                Spacer(Modifier.width(12.dp))

                Column(Modifier.fillMaxWidth()) {
                    when {
                        isLoading -> CircularProgressIndicator(modifier = Modifier.size(18.dp))

                        error != null -> Text(
                            text = "Failed to load dishes",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )

                        dishes.isEmpty() -> Text(
                            text = "No dishes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            dishes.forEach { d ->
                                Row(modifier = Modifier.fillMaxWidth().padding(0.dp)) {
                                    SingleLineText(
                                        d.dish.name,
                                        modifier = Modifier.weight(0.89f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        d.meal.time.toString(),
                                        modifier = Modifier.weight(0.11f).align(Alignment.CenterVertically),
                                        style = MaterialTheme.typography.labelSmall,
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
