package kz.ruccola.food.feature.subscription

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.choose_plan
import food.composeappcustomer.generated.resources.evening
import food.composeappcustomer.generated.resources.loading_plan
import food.composeappcustomer.generated.resources.morning
import food.composeappcustomer.generated.resources.morning_delivery
import food.composeappcustomer.generated.resources.needs_cutlery
import food.composeappcustomer.generated.resources.no_plan_selected
import food.composeappcustomer.generated.resources.no_plan_selected_description
import food.composeappcustomer.generated.resources.weekend_delivery_evening
import food.composeappcustomer.generated.resources.weekend_delivery_morning
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ToggleButtonsRow
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubscriptionContent(uiState: SubscriptionUiState, viewModel: SubscriptionViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
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
                        Text(stringResource(Res.string.loading_plan), style = MaterialTheme.typography.bodyLarge)
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
                    Text(stringResource(Res.string.no_plan_selected), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(Res.string.no_plan_selected_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(onClick = { viewModel.setShowPlanDialog(true) }) {
                        Text(stringResource(Res.string.choose_plan))
                    }
                }
            }

            else -> {
                SubscriptionPlanCard(
                    customerPlan = uiState.customerPlan!!,
                    onEditClick = { viewModel.setShowPlanDialog(true) },
                )
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

@Composable
private fun DeliveryPreferencesSection(viewModel: SubscriptionViewModel, uiState: SubscriptionUiState) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PreferenceSwitchRow(
                label = stringResource(Res.string.needs_cutlery),
                checked = uiState.needsCutlery,
                onCheckedChange = { viewModel.updateDeliveryPrefs(needsCutlery = it) },
            )
            HorizontalDivider()
            PreferenceSwitchRow(
                label =
                    if (uiState.morningDelivery) {
                        stringResource(Res.string.weekend_delivery_morning)
                    } else {
                        stringResource(Res.string.weekend_delivery_evening)
                    },
                checked = uiState.weekendDelivery,
                onCheckedChange = { viewModel.updateDeliveryPrefs(weekendDelivery = it) },
            )
            HorizontalDivider()
            Spacer(Modifier.height(2.dp))
            Text(stringResource(Res.string.morning_delivery), style = MaterialTheme.typography.bodyLarge)
            ToggleButtonsRow(
                listOf(stringResource(Res.string.morning), stringResource(Res.string.evening)),
                if (uiState.morningDelivery) 0 else 1,
                onSelectedIndexChange = { i -> viewModel.updateDeliveryPrefs(morningDelivery = i == 0) },
            )
            Spacer(Modifier)
        }
    }
}

@Composable
private fun PreferenceSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
