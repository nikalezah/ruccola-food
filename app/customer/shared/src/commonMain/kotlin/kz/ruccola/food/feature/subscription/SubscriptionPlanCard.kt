package kz.ruccola.food.feature.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.choose_plan
import food.composeappcustomer.generated.resources.days_quantity
import food.composeappcustomer.generated.resources.label_calories
import food.composeappcustomer.generated.resources.label_price_per_day
import food.composeappcustomer.generated.resources.label_total_price
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kz.ruccola.food.api.CustomerPlanDetailsDto
import kz.ruccola.food.ui.Icons
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubscriptionPlanCard(customerPlan: CustomerPlanDetailsDto, onEditClick: () -> Unit, modifier: Modifier = Modifier) {
    val startDate = customerPlan.chosenDate
    val days = customerPlan.days
    val endEpoch = startDate.toEpochDays() + (days - 1)
    val endDate = LocalDate.fromEpochDays(endEpoch)
    val totalPrice = customerPlan.pricePerDay * days

    val startDateText =
        startDate.day.toString().padStart(2, '0') + "." + startDate.month.number.toString().padStart(2, '0')
    val endDateText = endDate.day.toString().padStart(2, '0') + "." + endDate.month.number.toString().padStart(2, '0')
    val daysText = pluralStringResource(Res.plurals.days_quantity, days, days)

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "$startDateText — $endDateText ($daysText)", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Outlined.EditSquare,
                        contentDescription = stringResource(Res.string.choose_plan),
                    )
                }
            }
            HorizontalDivider()
            SubscriptionInfoRow(
                label = stringResource(Res.string.label_calories),
                value = customerPlan.calories.toString(),
            )
            SubscriptionInfoRow(
                label = stringResource(Res.string.label_price_per_day),
                value = customerPlan.pricePerDay.toString(),
            )
            SubscriptionInfoRow(
                label = stringResource(Res.string.label_total_price),
                value = totalPrice.toString(),
                valueColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
internal fun SubscriptionInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = valueColor)
    }
}
