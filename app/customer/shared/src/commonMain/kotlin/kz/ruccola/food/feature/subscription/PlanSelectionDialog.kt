package kz.ruccola.food.feature.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.cancel
import food.composeappcustomer.generated.resources.choose_plan
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.label_calories
import food.composeappcustomer.generated.resources.label_price_per_day
import food.composeappcustomer.generated.resources.label_total_price
import food.composeappcustomer.generated.resources.no_plans_available
import food.composeappcustomer.generated.resources.period_days
import food.composeappcustomer.generated.resources.save
import food.composeappcustomer.generated.resources.saving
import kz.ruccola.food.ui.Icons
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun PlanSelectionDialog(viewModel: SubscriptionViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.setShowPlanDialog(false) },
        title = { Text(stringResource(Res.string.choose_plan), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val pricePerDay = viewModel.effectivePricePerDay()
                val totalPrice = pricePerDay?.let { it * uiState.selectedDays }

                val pricePerDayText = pricePerDay?.toString() ?: "-"
                val totalPriceText = totalPrice?.toString() ?: "-"

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(Res.string.label_price_per_day),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                pricePerDayText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(Res.string.label_total_price),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                totalPriceText,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                if (uiState.caloriesOptions.isNotEmpty()) {
                    val maxIndex = (uiState.caloriesOptions.size - 1).coerceAtLeast(0)
                    val steps = if (maxIndex >= 1) maxIndex - 1 else 0
                    val shownCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "${stringResource(Res.string.label_calories)}: ${shownCalories?.toString() ?: "-"}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Icon(
                                    imageVector = Icons.Filled.DinnerDining,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Slider(
                                value = uiState.caloriesIndex.toFloat(),
                                onValueChange = { viewModel.setCaloriesIndex(it.roundToInt()) },
                                valueRange = 0f..maxIndex.toFloat(),
                                steps = steps,
                            )
                        }
                    }
                } else if (!uiState.isLoadingPlansForDialog) {
                    Text(stringResource(Res.string.no_plans_available))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${stringResource(Res.string.period_days)}: ${uiState.selectedDays}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Slider(
                            value = uiState.selectedDays.toFloat(),
                            onValueChange = { viewModel.setSelectedDays(it.roundToInt()) },
                            valueRange = 1f..30f,
                            steps = 28,
                        )
                    }
                }

                if (uiState.dialogError != null) {
                    Text(
                        stringResource(Res.string.error_prefix, uiState.dialogError!!),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            val selectedCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)
            val hasValidPlan =
                selectedCalories != null &&
                        uiState.allPlans.any {
                            it.calories.amount == selectedCalories && it.periodDays.amount <= uiState.selectedDays
                        }

            Button(onClick = { viewModel.savePlan() }, enabled = hasValidPlan && !uiState.isSavingPlan) {
                Text(if (uiState.isSavingPlan) stringResource(Res.string.saving) else stringResource(Res.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { viewModel.setShowPlanDialog(false) }, enabled = !uiState.isSavingPlan) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
