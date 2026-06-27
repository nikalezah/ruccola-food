package kz.ruccola.food.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.calories
import food.composeappadmin.generated.resources.cancel
import food.composeappadmin.generated.resources.create_plan
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.edit_plan
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.period_days
import food.composeappadmin.generated.resources.price_per_day_short
import food.composeappadmin.generated.resources.save
import food.composeappadmin.generated.resources.total_for_period
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun PlanEditorDialog(
    plan: PlanDto?,
    initialCalories: PlanCalories?,
    initialDays: PlanDays?,
    onDismiss: () -> Unit,
    onSave: (PlanCalories, PlanDays, Int) -> Unit,
    onDelete: (Int) -> Unit,
    onClearError: () -> Unit,
    isSaving: Boolean,
    error: String?,
) {
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
                val calOptions = remember { PlanCalories.entries.toList() }
                val selectedIndex = remember(calories) { calOptions.indexOf(calories).coerceAtLeast(0) }
                var sliderPos by remember { mutableStateOf(selectedIndex.toFloat()) }
                LaunchedEffect(calories) {
                    val idx = calOptions.indexOf(calories)
                    if (idx != sliderPos.toInt()) sliderPos = idx.toFloat()
                }

                if (plan == null) {
                    Text(
                        "${stringResource(Res.string.calories)}: ${planCalLabel(calories)}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = sliderPos,
                        onValueChange = { v ->
                            sliderPos = v
                            val idx = v.roundToInt().coerceIn(0, calOptions.lastIndex)
                            calories = calOptions[idx]
                            onClearError()
                        },
                        valueRange = 0f..calOptions.lastIndex.toFloat(),
                        steps = (calOptions.size - 2).coerceAtLeast(0),
                        enabled = !isSaving,
                    )
                    val dayOptions = remember { PlanDays.entries.toList() }
                    val selectedDayIndex = remember(days) { dayOptions.indexOf(days).coerceAtLeast(0) }
                    var daySliderPos by remember { mutableStateOf(selectedDayIndex.toFloat()) }
                    LaunchedEffect(days) {
                        val idx = dayOptions.indexOf(days)
                        if (idx != daySliderPos.toInt()) daySliderPos = idx.toFloat()
                    }

                    Text(
                        "${stringResource(Res.string.period_days)}: ${planDaysLabel(days)}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = daySliderPos,
                        onValueChange = { v ->
                            daySliderPos = v
                            val idx = v.roundToInt().coerceIn(0, dayOptions.lastIndex)
                            days = dayOptions[idx]
                            onClearError()
                        },
                        valueRange = 0f..dayOptions.lastIndex.toFloat(),
                        steps = (dayOptions.size - 2).coerceAtLeast(0),
                        enabled = !isSaving,
                    )
                } else {
                    Text(
                        "${stringResource(Res.string.calories)}: ${planCalLabel(calories)}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        "${stringResource(Res.string.period_days)}: ${planDaysLabel(days)}",
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
                        stringResource(Res.string.total_for_period, (it * days.amount).toString()),
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
