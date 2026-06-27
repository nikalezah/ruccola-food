package kz.ruccola.food.feature.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.kcal_days
import food.composeappadmin.generated.resources.no_plans
import food.composeappadmin.generated.resources.retry
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.ui.PullToRefresh
import kz.ruccola.food.ui.scaffold.AsyncContent
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlanContent(
    state: PlanUiState,
    onRefresh: () -> Unit,
    onCellClick: (PlanDto) -> Unit,
    onEmptyCellClick: (PlanCalories, PlanDays) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ptrState = rememberPullToRefreshState()
    val thresholdPx = with(LocalDensity.current) { 100.dp.toPx() }

    PullToRefresh(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = modifier,
        state = ptrState,
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).graphicsLayer {
                translationY = ptrState.distanceFraction * thresholdPx
            },
        ) {
            AsyncContent(
                isLoading = state.isLoading,
                error = state.error,
                isEmpty = state.items.isEmpty(),
                onRetry = onRefresh,
                errorText = stringResource(Res.string.error_prefix, state.error ?: ""),
                emptyText = stringResource(Res.string.no_plans),
                retryLabel = stringResource(Res.string.retry),
            ) {
                PlansTable(
                    items = state.items,
                    onCellClick = onCellClick,
                    onEmptyCellClick = onEmptyCellClick,
                )
            }
        }
    }
}

@Composable
private fun PlansTable(
    items: List<PlanDto>,
    onCellClick: (PlanDto) -> Unit,
    onEmptyCellClick: (PlanCalories, PlanDays) -> Unit,
) {
    val caloriesList = remember(items) { items.map { it.calories }.distinct().sortedBy { it.ordinal } }
    val periods = remember(items) { items.map { it.periodDays }.distinct().sortedBy { it.ordinal } }
    val map = remember(items) { items.associateBy { it.calories to it.periodDays } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(72.dp)) {
                Text(
                    stringResource(Res.string.kcal_days),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            periods.forEach { d ->
                Box(Modifier.weight(1f), Alignment.TopCenter) {
                    Text(planDaysLabel(d), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        caloriesList.forEach { cal ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(72.dp)) { Text(planCalLabel(cal), style = MaterialTheme.typography.bodyMedium) }
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
                            PlanCard(plan = plan, onClick = { onCellClick(plan) })
                        }
                    }
                }
            }
            HorizontalDivider()
        }
    }
}
