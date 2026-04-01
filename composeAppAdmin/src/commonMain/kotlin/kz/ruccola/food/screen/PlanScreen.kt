package kz.ruccola.food.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.calories
import food.composeappadmin.generated.resources.cancel
import food.composeappadmin.generated.resources.create_plan
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.edit_plan
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.kcal_days
import food.composeappadmin.generated.resources.no_plans
import food.composeappadmin.generated.resources.period_days
import food.composeappadmin.generated.resources.price_per_day_short
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.save
import food.composeappadmin.generated.resources.tab_plans
import food.composeappadmin.generated.resources.total_for_period
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.PullToRefresh
import kz.ruccola.food.ui.ToggleButtonsRow
import kz.ruccola.food.viewmodel.PlanViewModel
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    val vm: PlanViewModel = viewModel(factory = PlanViewModel.Factory)
    val state by vm.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<PlanDto?>(null) }

    var prefillCalories by remember { mutableStateOf<PlanCalories?>(null) }
    var prefillDays by remember { mutableStateOf<PlanDays?>(null) }

    val ptrState = rememberPullToRefreshState()
    val thresholdPx = with(LocalDensity.current) { 100.dp.toPx() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tab_plans)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingPlan = null
                prefillCalories = null
                prefillDays = null
                showEditor = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.add))
            }
        },
    ) { padding ->
        PullToRefresh(
            isRefreshing = state.isLoading,
            onRefresh = { vm.loadAll() },
            modifier = Modifier.fillMaxSize().padding(padding),
            state = ptrState,
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).graphicsLayer {
                    translationY = ptrState.distanceFraction * thresholdPx
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    if (state.error != null && state.items.isEmpty()) {
                        Column(
                            Modifier.align(Alignment.Center).padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(Res.string.error_prefix, state.error ?: ""),
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { vm.loadAll() }) { Text(stringResource(Res.string.retry)) }
                        }
                    } else {
                        PlansTable(
                            items = state.items,
                            onCellClick = { plan ->
                                prefillCalories = null
                                prefillDays = null
                                editingPlan = plan
                                showEditor = true
                            },
                            onEmptyCellClick = { cal, d ->
                                editingPlan = null
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
            initialCalories = prefillCalories,
            initialDays = prefillDays,
            onDismiss = {
                showEditor = false
                vm.clearError()
            },
            onSave = { cals, days, ppd ->
                if (editingPlan == null) {
                    vm.create(cals, days, ppd)
                } else {
                    vm.update(editingPlan!!.id, cals, days, ppd)
                }
                showEditor = false
            },
            onDelete = { id ->
                vm.delete(id)
                showEditor = false
            },
            isSaving = state.isSaving,
            error = state.error,
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

    val caloriesList = remember(items) { items.map { it.calories }.distinct().sortedBy { it.ordinal } }
    val periods = remember(items) { items.map { it.periodDays }.distinct().sortedBy { it.ordinal } }
    val map = remember(items) { items.associateBy { it.calories to it.periodDays } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize()) { Text(stringResource(Res.string.no_plans), Modifier.align(Alignment.Center)) }
            return@Column
        }
        // Header row
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(72.dp)) {
                Text(
                    stringResource(Res.string.kcal_days),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
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
                                    Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    initialCalories: PlanCalories?,
    initialDays: PlanDays?,
    onDismiss: () -> Unit,
    onSave: (PlanCalories, PlanDays, Int) -> Unit,
    onDelete: (Int) -> Unit,
    isSaving: Boolean,
    error: String?,
) {
    fun calLabel(c: PlanCalories) = c.name.drop(1)

    fun daysLabel(d: PlanDays) = d.amount.toString()

    fun daysInt(d: PlanDays) = d.amount

    var calories by remember { mutableStateOf(plan?.calories ?: (initialCalories ?: PlanCalories.C900)) }
    var days by remember { mutableStateOf(plan?.periodDays ?: (initialDays ?: PlanDays.D1)) }
    var ppd by remember { mutableStateOf(plan?.pricePerDay?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (plan == null) stringResource(Res.string.create_plan) else stringResource(Res.string.edit_plan))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Calorie slider
                val calOptions = remember { PlanCalories.entries.toList() }
                val selectedIndex = remember(calories) { calOptions.indexOf(calories).coerceAtLeast(0) }
                var sliderPos by remember { mutableStateOf(selectedIndex.toFloat()) }
                // Keep slider and enum in sync
                LaunchedEffect(calories) {
                    val idx = calOptions.indexOf(calories)
                    if (idx != sliderPos.toInt()) sliderPos = idx.toFloat()
                }

                if (plan == null) {
                    Text(
                        "${stringResource(Res.string.calories)}: ${calLabel(calories)}",
                        style = MaterialTheme.typography.labelLarge,
                    )
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
                    Text(stringResource(Res.string.period_days), style = MaterialTheme.typography.labelLarge)
                    ToggleButtonsRow(
                        options = PlanDays.entries.map { it.amount.toString() },
                        initialSelectedIndex = PlanDays.entries.indexOf(days),
                        onSelectedIndexChange = { i: Int -> days = PlanDays.entries[i] },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        "${stringResource(Res.string.calories)}: ${calLabel(calories)}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "${stringResource(Res.string.period_days)}: ${daysLabel(days)}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                OutlinedTextField(
                    value = ppd,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }
                        ppd = filtered
                    },
                    label = { Text(stringResource(Res.string.price_per_day_short)) },
                    singleLine = true,
                    enabled = !isSaving,
                )

                ppd.toIntOrNull()?.also {
                    Text(
                        stringResource(Res.string.total_for_period, (it * daysInt(days)).toString()),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                if (error != null) {
                    Text(stringResource(Res.string.error_prefix, error), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = ppd.toIntOrNull() ?: return@Button
                    onSave(calories, days, price)
                },
                enabled = !isSaving && ppd.isNotEmpty(),
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (plan != null) {
                    TextButton(
                        onClick = { onDelete(plan.id) },
                        enabled = !isSaving,
                    ) {
                        Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(Res.string.cancel)) }
            }
        },
    )
}
