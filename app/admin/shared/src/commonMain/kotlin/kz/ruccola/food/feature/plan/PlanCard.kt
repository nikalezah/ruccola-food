package kz.ruccola.food.feature.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays

@Composable
fun PlanCard(
    plan: PlanDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodDays = plan.periodDays.amount

    OutlinedCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${plan.pricePerDay}", style = MaterialTheme.typography.bodySmall)
            }
            if (periodDays > 1) {
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(Modifier.height(4.dp))
                val total = plan.pricePerDay * periodDays
                Text(
                    "$total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun planCalLabel(calories: PlanCalories) = calories.name.drop(1)

internal fun planDaysLabel(days: PlanDays) = days.amount.toString()
