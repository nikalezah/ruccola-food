package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.cancel
import food.composeappcustomer.generated.resources.choose_plan
import food.composeappcustomer.generated.resources.chosen_plan_title
import food.composeappcustomer.generated.resources.days_quantity
import food.composeappcustomer.generated.resources.delivery_prefs_title
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.evening
import food.composeappcustomer.generated.resources.format_kcal
import food.composeappcustomer.generated.resources.label_calories
import food.composeappcustomer.generated.resources.label_end_date
import food.composeappcustomer.generated.resources.label_price_per_day
import food.composeappcustomer.generated.resources.label_start_date
import food.composeappcustomer.generated.resources.label_total_price
import food.composeappcustomer.generated.resources.loading_plan
import food.composeappcustomer.generated.resources.morning
import food.composeappcustomer.generated.resources.morning_delivery
import food.composeappcustomer.generated.resources.needs_cutlery
import food.composeappcustomer.generated.resources.no_plan_selected
import food.composeappcustomer.generated.resources.no_plan_selected_description
import food.composeappcustomer.generated.resources.no_plans_available
import food.composeappcustomer.generated.resources.period_days
import food.composeappcustomer.generated.resources.save
import food.composeappcustomer.generated.resources.saving
import food.composeappcustomer.generated.resources.tab_subscription
import food.composeappcustomer.generated.resources.weekend_delivery
import kotlinx.datetime.LocalDate
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ToggleButtonsRow
import kz.ruccola.food.viewmodel.SubscriptionUiState
import kz.ruccola.food.viewmodel.SubscriptionViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(viewModel: SubscriptionViewModel = viewModel { SubscriptionViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tab_subscription)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                uiState.isLoadingPlan -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            Text(
                                stringResource(Res.string.loading_plan),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                uiState.customerPlan == null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DinnerDining,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(Res.string.no_plan_selected),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(Res.string.no_plan_selected_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = {
                            viewModel.setShowPlanDialog(true)
                        }) { Text(stringResource(Res.string.choose_plan)) }
                    }
                }

                else -> {
                    val customerPlan = uiState.customerPlan!!
                    val startDate = customerPlan.chosenDate
                    val days = customerPlan.days
                    val endEpoch = startDate.toEpochDays() + (days - 1)
                    val endDate = LocalDate.fromEpochDays(endEpoch)
                    val totalPrice = customerPlan.pricePerDay * days

                    val kcalText = stringResource(Res.string.format_kcal, customerPlan.calories.toString())
                    val totalPriceText = totalPrice.toString()
                    val startDateText = startDate.toString()
                    val endDateText = endDate.toString()

                    val daysText = pluralStringResource(Res.plurals.days_quantity, days, days)

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.setShowPlanDialog(true) },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                stringResource(Res.string.chosen_plan_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            PlanInfoRow(
                                icon = Icons.Filled.DinnerDining,
                                label = stringResource(Res.string.label_calories, kcalText),
                            )
                            PlanInfoRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = daysText,
                            )
                            PlanInfoRow(
                                icon = Icons.Filled.PriceChange,
                                label = stringResource(Res.string.label_total_price, totalPriceText),
                                valueColor = MaterialTheme.colorScheme.primary,
                            )
                            PlanInfoRow(
                                icon = Icons.Filled.Today,
                                label = stringResource(Res.string.label_start_date, startDateText),
                            )
                            PlanInfoRow(
                                icon = Icons.Filled.Today,
                                label = stringResource(Res.string.label_end_date, endDateText),
                            )
                        }
                    }
                }
            }

            if (uiState.showPlanDialog) {
                PlanSelectionDialog(viewModel = viewModel)
            }

            if (uiState.customerPlan != null) {
                DeliveryPreferencesSection(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

@Composable
private fun PlanInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
        )
    }
}

@Composable
private fun DeliveryPreferencesSection(
    viewModel: SubscriptionViewModel,
    uiState: SubscriptionUiState,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(Res.string.delivery_prefs_title),
                style = MaterialTheme.typography.titleLarge,
            )
            HorizontalDivider()
            PreferenceSwitchRow(
                icon = Icons.Outlined.Settings,
                label = stringResource(Res.string.needs_cutlery),
                checked = uiState.needsCutlery,
                onCheckedChange = { viewModel.updateDeliveryPrefs(needsCutlery = it) },
            )
            HorizontalDivider()
            PreferenceSwitchRow(
                icon = Icons.Filled.CalendarMonth,
                label = stringResource(Res.string.weekend_delivery),
                checked = uiState.weekendDelivery,
                onCheckedChange = { viewModel.updateDeliveryPrefs(weekendDelivery = it) },
            )
            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.morning_delivery),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ToggleButtonsRow(
                    listOf(
                        stringResource(Res.string.morning),
                        stringResource(Res.string.evening),
                    ),
                    if (uiState.morningDelivery) 0 else 1,
                    onSelectedIndexChange = { i ->
                        viewModel.updateDeliveryPrefs(morningDelivery = i == 0)
                    },
                )
            }
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun PlanSelectionDialog(viewModel: SubscriptionViewModel) {
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
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
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(Res.string.label_calories, shownCalories?.toString() ?: "-"),
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
            val hasValidPlan = selectedCalories != null && uiState.allPlans.any {
                it.calories.amount == selectedCalories && it.periodDays.amount <= uiState.selectedDays
            }

            Button(
                onClick = { viewModel.savePlan() },
                enabled = hasValidPlan && !uiState.isSavingPlan,
            ) {
                Text(if (uiState.isSavingPlan) stringResource(Res.string.saving) else stringResource(Res.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.setShowPlanDialog(false) },
                enabled = !uiState.isSavingPlan,
            ) { Text(stringResource(Res.string.cancel)) }
        },
    )
}
