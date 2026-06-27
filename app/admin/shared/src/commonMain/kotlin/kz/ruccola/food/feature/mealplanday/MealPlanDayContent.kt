package kz.ruccola.food.feature.mealplanday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import food.composeappadmin.generated.resources.no_items
import food.composeappadmin.generated.resources.retry
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.PullToRefresh
import org.jetbrains.compose.resources.stringResource

@Composable
fun MealPlanDayContent(
    state: MealPlanDayUiState,
    workingList: List<MealPlanDayDto>,
    reorderMode: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (MealPlanDayDto) -> Unit,
    onDelete: (Int) -> Unit,
    onMakeCurrent: (Int) -> Unit,
    onMoveUp: (MealPlanDayDto) -> Unit,
    onMoveDown: (MealPlanDayDto) -> Unit,
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
        when {
            state.error != null -> {
                Column(
                    Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.error_prefix, state.error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRefresh) { Text(stringResource(Res.string.retry)) }
                }
            }

            !state.isLoading && state.items.isEmpty() -> {
                Column(
                    Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(Res.string.no_items))
                }
            }

            else -> {
                val originalPositions = remember(state.items) {
                    state.items.mapIndexed { index, day -> day.id to (index + 1) }.toMap()
                }
                LazyColumn(
                    Modifier.fillMaxSize().padding(16.dp).graphicsLayer {
                        translationY = ptrState.distanceFraction * thresholdPx
                    },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(workingList, key = { _, it -> it.id }) { index, day ->
                        val previousPosition = originalPositions[day.id] ?: (index + 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                MealPlanDayItem(
                                    item = day,
                                    previousPosition = previousPosition,
                                    currentPosition = index + 1,
                                    showReorderPosition = reorderMode,
                                    swipeEnabled = !reorderMode,
                                    onClick = { onItemClick(day) },
                                    onDelete = { onDelete(day.id) },
                                    onMakeCurrent = { onMakeCurrent(day.id) },
                                )
                            }
                            if (reorderMode) {
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    IconButton(
                                        onClick = { onMoveUp(day) },
                                        enabled = workingList.indexOf(day) > 0,
                                    ) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Up") }
                                    IconButton(
                                        onClick = { onMoveDown(day) },
                                        enabled = workingList.indexOf(day) < workingList.size - 1,
                                    ) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Down") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
