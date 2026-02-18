package kz.ruccola.food.admin.screens

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayApi
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.ui.SingleLineText
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanDayScreen() {
    var mealPlanDays by remember { mutableStateOf<List<MealPlanDayDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editingDay by remember { mutableStateOf<MealPlanDayDto?>(null) }
    var nextSerial by remember { mutableIntStateOf(1) }
    var reorderMode by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { MealPlanDayApi() }

    // Local working copy for reordering
    val workingList = remember { mutableStateListOf<MealPlanDayDto>() }
    LaunchedEffect(mealPlanDays) {
        workingList.clear()
        workingList.addAll(mealPlanDays)
    }

    fun isOrderChanged(): Boolean {
        val originalIds = mealPlanDays.map { it.id }
        val workingIds = workingList.map { it.id }
        return originalIds != workingIds
    }

    fun loadMealPlanDays() {
        scope.launch {
            isLoading = true
            error = null
            try {
                mealPlanDays = api.getAll()
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки дней плана"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadMealPlanDays() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(Strings.tabMealPlanDays) },
                navigationIcon = {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                },
                actions = {
                    IconButton(onClick = { loadMealPlanDays() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    FilledIconToggleButton(
                        checked = reorderMode,
                        onCheckedChange = { checked ->
                            if (reorderMode) {
                                scope.launch {
                                    try {
                                        if (isOrderChanged()) {
                                            api.reorder(workingList.map { it.id })
                                            loadMealPlanDays()
                                        }
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            }
                            reorderMode = checked
                        },
                    ) {
                        if (reorderMode) {
                            Icon(Icons.Default.Check, contentDescription = "Done")
                        } else {
                            Icon(Icons.Default.SwapVert, contentDescription = "Reorder")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nextSerial = (mealPlanDays.maxOfOrNull { it.serial } ?: 0) + 1
                    editingDay = null
                    showEditor = true
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { loadMealPlanDays() }) { Text(Strings.retry) }
                    }
                }

                mealPlanDays.isEmpty() -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Дни плана не найдены")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            editingDay = null
                            showEditor = true
                        }) { Text("Добавить день") }
                    }
                }

                else -> {
                    val originalPositions = remember(mealPlanDays) {
                        mealPlanDays.mapIndexed { index, day -> day.id to (index + 1) }.toMap()
                    }
                    LazyColumn(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(workingList) { index, day ->
                            val previousPosition = originalPositions[day.id] ?: (index + 1)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f)) {
                                    MealPlanDayItem(
                                        item = day,
                                        previousPosition = previousPosition,
                                        currentPosition = index + 1,
                                        showReorderPosition = reorderMode,
                                        enableSwipeDelete = !reorderMode,
                                        onClick = {
                                            editingDay = day
                                            showEditor = true
                                        },
                                        onDelete = {
                                            scope.launch {
                                                try {
                                                    api.delete(day.id)
                                                    loadMealPlanDays()
                                                } catch (e: Exception) {
                                                    error = e.message
                                                }
                                            }
                                        },
                                        onMakeCurrent = {
                                            scope.launch {
                                                try {
                                                    api.setCurrent(day.id)
                                                    loadMealPlanDays()
                                                } catch (e: Exception) {
                                                    error = e.message
                                                }
                                            }
                                        },
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
                                        ) { Icon(Icons.Default.ArrowUpward, contentDescription = "Up") }
                                        IconButton(
                                            onClick = {
                                                val idx = workingList.indexOf(day)
                                                if (idx < workingList.size - 1) {
                                                    workingList.removeAt(idx)
                                                    workingList.add(idx + 1, day)
                                                }
                                            },
                                            enabled = workingList.indexOf(day) < workingList.size - 1,
                                        ) { Icon(Icons.Default.ArrowDownward, contentDescription = "Down") }
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
            initialItem = editingDay,
            nextSerial = nextSerial,
            onClose = {
                showEditor = false
                editingDay = null
                loadMealPlanDays()
            },
        )
    }

    if (showHistory) {
        DayScreen(onClose = { showHistory = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanDayItem(
    item: MealPlanDayDto,
    previousPosition: Int,
    currentPosition: Int,
    showReorderPosition: Boolean,
    enableSwipeDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMakeCurrent: () -> Unit,
) {
    val api = remember { MealPlanDayApi() }
    var dishes by remember { mutableStateOf<List<DishWithMealDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 80.dp.toPx() }
    val swipeReady = kotlin.math.abs(dragOffset) >= swipeThresholdPx
    val swipeIconTint = if (swipeReady) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(item.id) {
        isLoading = true
        error = null
        try {
            dishes = api.getDishes(item.id)
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    LaunchedEffect(enableSwipeDelete) {
        if (!enableSwipeDelete) {
            dragOffset = 0f
        }
    }

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp)
                .heightIn(min = 142.dp),
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
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
                .then(
                    if (enableSwipeDelete) {
                        Modifier.pointerInput(item.id) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                },
                                onDragEnd = {
                                    if (kotlin.math.abs(dragOffset) >= swipeThresholdPx) {
                                        onDelete()
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
                            Icons.Default.ArrowDownward,
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
                VerticalDivider()
                Spacer(Modifier.width(12.dp))

                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
