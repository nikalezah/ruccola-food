package kz.ruccola.food.admin.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.PlanApi
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.web.common.ui.FabMenu
import kz.ruccola.food.web.common.ui.ToggleButtonsRow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    var plans by remember { mutableStateOf<List<PlanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<PlanDto?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var createAllowVariant by remember { mutableStateOf(false) }
    var prefillCalories by remember { mutableStateOf<PlanCalories?>(null) }
    var prefillDays by remember { mutableStateOf<PlanDays?>(null) }

    val scope = rememberCoroutineScope()
    val planApi = remember { PlanApi() }

    fun loadPlans() {
        scope.launch {
            isLoading = true
            error = null
            try {
                plans = planApi.getAll()
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки планов"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadPlans() }

    val noVariants = remember(plans) { plans.filter { !it.allowVariantChoice } }
    val withVariants = remember(plans) { plans.filter { it.allowVariantChoice } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(Strings.tabPlans) },
                actions = {
                    IconButton(onClick = { loadPlans() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        floatingActionButton = {
            val onClick = {
                editingPlan = null
                prefillCalories = null
                prefillDays = null
                showEditor = true
            }
            FabMenu(
                listOf(
                    Triple(null, "Новая цена без вариантов") {
                        onClick()
                        createAllowVariant = false
                    },
                    Triple(null, "Новая цена с вариантами") {
                        onClick()
                        createAllowVariant = true
                    },
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Без вариантов") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("С вариантами") })
            }

            Box(Modifier.fillMaxSize()) {
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
                            Button(onClick = { loadPlans() }) { Text(Strings.retry) }
                        }
                    }

                    else -> {
                        val itemsToShow = if (selectedTab == 0) noVariants else withVariants
                        PlansTable(
                            items = itemsToShow,
                            onCellClick = { plan ->
                                prefillCalories = null
                                prefillDays = null
                                editingPlan = plan
                                showEditor = true
                            },
                            onEmptyCellClick = { cal, d ->
                                editingPlan = null
                                createAllowVariant = selectedTab == 1
                                prefillCalories = cal
                                prefillDays = d
                                showEditor = true
                            },
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        PlanEditorDialog(
            plan = editingPlan,
            initialAllowForCreate = if (editingPlan == null) createAllowVariant else null,
            initialCalories = prefillCalories,
            initialDays = prefillDays,
            onDismiss = {
                showEditor = false
                editingPlan = null
                prefillCalories = null
                prefillDays = null
            },
            onSave = {
                showEditor = false
                editingPlan = null
                prefillCalories = null
                prefillDays = null
                loadPlans()
            },
            onDelete = {
                showEditor = false
                editingPlan = null
                prefillCalories = null
                prefillDays = null
                loadPlans()
            },
            planApi = planApi,
        )
    }
}

@Composable
fun PlansTable(
    items: List<PlanDto>,
    onCellClick: (PlanDto) -> Unit,
    onEmptyCellClick: (PlanCalories, PlanDays) -> Unit,
) {
    fun calLabel(c: PlanCalories) = c.name.drop(1)

    fun daysLabel(d: PlanDays) = d.amount.toString()

    fun daysInt(d: PlanDays) = d.amount
    // Prepare distinct headers
    val caloriesList = remember(items) { items.map { it.calories }.distinct().sortedBy { it.ordinal } }
    val periods = remember(items) { items.map { it.periodDays }.distinct().sortedBy { it.ordinal } }
    val map = remember(items) { items.associateBy { it.calories to it.periodDays } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize()) { Text("Нет планов", Modifier.align(Alignment.Center)) }
            return@Column
        }
        // Header row
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(72.dp)) { Text("ккал\\дни", style = MaterialTheme.typography.labelMedium) }
            periods.forEach { d ->
                Box(Modifier.weight(1f), Alignment.TopCenter) {
                    Text(daysLabel(d), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        // Rows
        caloriesList.forEach { cal ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(72.dp)) { Text(calLabel(cal), style = MaterialTheme.typography.bodyMedium) }
                periods.forEach { d ->
                    val plan = map[cal to d]
                    Box(
                        Modifier.weight(1f).padding(4.dp),
                    ) {
                        if (plan == null) {
                            Surface(
                                tonalElevation = 2.dp,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.clickable { onEmptyCellClick(cal, d) },
                            ) {
                                Box(Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(
                                        "—",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { onCellClick(plan) }) {
                                Column(Modifier.padding(8.dp)) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${plan.pricePerDay}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (daysInt(plan.periodDays) > 1) {
                                        Spacer(Modifier.height(4.dp))
                                        HorizontalDivider(thickness = 0.5.dp)
                                        Spacer(Modifier.height(4.dp))
                                        val total = plan.pricePerDay * daysInt(plan.periodDays)
                                        Text(
                                            "$total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun PlanEditorDialog(
    plan: PlanDto?,
    initialAllowForCreate: Boolean?,
    initialCalories: PlanCalories?,
    initialDays: PlanDays?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    planApi: PlanApi,
) {
    fun calLabel(c: PlanCalories) = c.name.drop(1)

    fun daysLabel(d: PlanDays) = d.amount.toString()

    fun daysInt(d: PlanDays) = d.amount

    var calories by remember { mutableStateOf(plan?.calories ?: (initialCalories ?: PlanCalories.C900)) }
    var days by remember { mutableStateOf(plan?.periodDays ?: (initialDays ?: PlanDays.D1)) }
    var ppd by remember { mutableStateOf(plan?.pricePerDay?.toString() ?: "") }
    var allow by remember { mutableStateOf(plan?.allowVariantChoice ?: (initialAllowForCreate ?: false)) }

    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun save() {
        val price = ppd.toIntOrNull() ?: return
        scope.launch {
            isSaving = true
            error = null
            try {
                if (plan == null) {
                    planApi.create(PlanCreateDto(calories, days, price, allow))
                } else {
                    planApi.update(plan.id, PlanUpdateDto(calories, days, price, allow))
                }
                onSave()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isSaving = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (plan == null) {
                    "Создать план — " + (
                        if ((
                                initialAllowForCreate
                                    ?: false
                            )
                        ) {
                            "С вариантами"
                        } else {
                            "Без вариантов"
                        }
                    )
                } else {
                    "Редактировать план"
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Calories slider
                val calOptions = remember { PlanCalories.entries.toList() }
                val selectedIndex = remember(calories) { calOptions.indexOf(calories).coerceAtLeast(0) }
                var sliderPos by remember { mutableStateOf(selectedIndex.toFloat()) }
                // Keep slider and enum in sync
                LaunchedEffect(calories) {
                    val idx = calOptions.indexOf(calories)
                    if (idx != sliderPos.toInt()) sliderPos = idx.toFloat()
                }

                if (plan == null) {
                    Text("Калории: ${calLabel(calories)}", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = sliderPos,
                        onValueChange = { v ->
                            sliderPos = v
                            val idx = v.roundToInt().coerceIn(0, calOptions.lastIndex)
                            calories = calOptions[idx]
                        },
                        valueRange = 0f..calOptions.lastIndex.toFloat(),
                        steps = (calOptions.size - 2).coerceAtLeast(0),
                        enabled = !isSaving,
                    )
                    Text("Дней", style = MaterialTheme.typography.labelLarge)
                    ToggleButtonsRow(
                        options = PlanDays.entries.map { it.amount.toString() },
                        initialSelectedIndex = PlanDays.entries.indexOf(days),
                        onSelectedIndexChange = { i: Int -> days = PlanDays.entries[i] },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text("Калории: ${calLabel(calories)}", style = MaterialTheme.typography.labelLarge)
                    Text("Дней: ${daysLabel(days)}", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedTextField(
                    value = ppd,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }
                        ppd = filtered
                    },
                    label = { Text("Цена за день") },
                    singleLine = true,
                    enabled = !isSaving,
                )

                ppd.toIntOrNull()?.also {
                    Text("Всего за период: ${it * daysInt(days)}", style = MaterialTheme.typography.labelLarge)
                }

                if (error != null) Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = { save() }, enabled = !isSaving && ppd.isNotEmpty()) { Text(Strings.save) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (plan != null) {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                planApi.delete(plan.id)
                                onDelete()
                            } catch (e: Exception) {
                                error = e.message
                            }
                        }
                    }, enabled = !isSaving) { Text(Strings.delete, color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss, enabled = !isSaving) { Text(Strings.cancel) }
            }
        },
    )
}
