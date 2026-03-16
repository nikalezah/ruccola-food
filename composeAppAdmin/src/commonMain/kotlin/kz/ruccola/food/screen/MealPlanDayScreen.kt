package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.no_data
import food.composeappadmin.generated.resources.no_items
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.screen_history_title
import food.composeappadmin.generated.resources.tab_schedule
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeToRemove
import kz.ruccola.food.viewmodel.MealPlanDayViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MealPlanDayScreen(onHistoryClick: () -> Unit) {
    val vm: MealPlanDayViewModel = viewModel(factory = MealPlanDayViewModel.Factory)
    val state by vm.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingDay by remember { mutableStateOf<MealPlanDayDto?>(null) }
    var nextSerial by remember { mutableIntStateOf(1) }
    var reorderMode by remember { mutableStateOf(false) }

    // Local working copy for reordering
    val workingList = remember { mutableStateListOf<MealPlanDayDto>() }
    LaunchedEffect(state.items) {
        workingList.clear()
        workingList.addAll(state.items)
    }

    fun isOrderChanged(): Boolean {
        val originalIds = state.items.map { it.id }
        val workingIds = workingList.map { it.id }
        return originalIds != workingIds
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tab_schedule)) },
                navigationIcon = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = stringResource(Res.string.screen_history_title),
                        )
                    }
                },
                actions = {
                    FilledIconToggleButton(
                        checked = reorderMode,
                        onCheckedChange = { checked ->
                            if (reorderMode) {
                                if (isOrderChanged()) {
                                    vm.reorder(workingList.map { it.id })
                                }
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
                    editingDay = null
                    showEditor = true
                },
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { vm.loadAll() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(Res.string.error_prefix, state.error ?: ""),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.loadAll() }) { Text(stringResource(Res.string.retry)) }
                    }
                }

                state.items.isEmpty() -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.no_items))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            editingDay = null
                            showEditor = true
                        }) { Text(stringResource(Res.string.add)) }
                    }
                }

                else -> {
                    val originalPositions = remember(state.items) {
                        state.items.mapIndexed { index, day -> day.id to (index + 1) }.toMap()
                    }
                    LazyColumn(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(workingList, key = { _, it -> it.id }) { index, day ->
                            val previousPosition = originalPositions[day.id] ?: (index + 1)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f)) {
                                    MealPlanDayItem(
                                        item = day,
                                        previousPosition = previousPosition,
                                        currentPosition = index + 1,
                                        showReorderPosition = reorderMode,
                                        swipeEnabled = !reorderMode,
                                        onClick = {
                                            editingDay = day
                                            showEditor = true
                                        },
                                        onDelete = { vm.delete(day.id) },
                                        onMakeCurrent = { vm.setCurrent(day.id) },
                                    )
                                }
                                if (reorderMode) {
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        IconButton(
                                            onClick = {
                                                val idx = workingList.indexOf(day)
                                                if (idx > 0) {
                                                    workingList.removeAt(idx)
                                                    workingList.add(idx - 1, day)
                                                }
                                            },
                                            enabled = workingList.indexOf(day) > 0,
                                        ) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Up") }
                                        IconButton(
                                            onClick = {
                                                val idx = workingList.indexOf(day)
                                                if (idx < workingList.size - 1) {
                                                    workingList.removeAt(idx)
                                                    workingList.add(idx + 1, day)
                                                }
                                            },
                                            enabled = workingList.indexOf(day) < workingList.size - 1,
                                        ) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Down") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        MealPlanDayEditorScreen(
            mealPlanDay = editingDay,
            nextSerial = nextSerial,
            onClose = {
                showEditor = false
                editingDay = null
                vm.loadAll()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanDayItem(
    item: MealPlanDayDto,
    previousPosition: Int,
    currentPosition: Int,
    showReorderPosition: Boolean,
    swipeEnabled: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMakeCurrent: () -> Unit,
) {
    var dishes by remember { mutableStateOf(item.dishes) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(item.dishes) {
        dishes = item.dishes
    }

    SwipeToRemove(
        Icons.Filled.Delete,
        stringResource(Res.string.delete),
        onDelete,
        CardDefaults.outlinedShape,
        swipeEnabled,
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(PaddingValues(0.dp, 8.dp, 8.dp, 8.dp))
                    .heightIn(min = 142.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp, 0.dp).width(48.dp).heightIn(min = 142.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("$previousPosition", style = MaterialTheme.typography.titleMedium)
                    if (showReorderPosition && currentPosition != previousPosition) {
                        Icon(
                            Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "$currentPosition",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (item.current) {
                        FilledIconButton(onClick = {}, enabled = false) {
                            Icon(Icons.Filled.Today, contentDescription = "Current")
                        }
                    } else {
                        IconButton(onClick = onMakeCurrent, enabled = !item.current) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = "Current")
                        }
                    }
                }
                VerticalDivider() // todo: find out why divider is invisible and fix it
                Spacer(Modifier.width(12.dp))

                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    when {
                        isLoading -> CircularProgressIndicator(modifier = Modifier.size(18.dp))

                        dishes.isEmpty() -> Text(
                            text = stringResource(Res.string.no_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            dishes.forEach { d ->
                                Row(modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
                                    SingleLineText(
                                        d.dish.name,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        d.meal.time.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(start = 8.dp),
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
