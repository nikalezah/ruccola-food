package kz.ruccola.food.feature.mealplanday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.no_data
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeToRemove
import org.jetbrains.compose.resources.stringResource

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
                VerticalDivider()
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
