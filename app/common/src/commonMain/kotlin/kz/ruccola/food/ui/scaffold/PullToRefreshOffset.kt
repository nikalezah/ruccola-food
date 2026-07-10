package kz.ruccola.food.ui.scaffold

import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.pullToRefreshOffset(ptrState: PullToRefreshState, threshold: Dp = 100.dp): Modifier {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    return graphicsLayer { translationY = ptrState.distanceFraction * thresholdPx }
}
