package kz.ruccola.food.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun PullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier,
    state: PullToRefreshState,
    contentAlignment: Alignment,
    indicator: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val threshold: Dp = 100.dp
    val thresholdPx = with(density) { threshold.toPx() }

    var dragPx by remember { mutableFloatStateOf(0f) }
    var didTrigger by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            dragPx = 0f
            didTrigger = 0f
        }
    }

    Box(
        modifier = modifier.pointerInput(isRefreshing) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dragAmount ->
                    if (!isRefreshing && dragAmount > 0f) {
                        change.consume()
                        dragPx = maxOf(0f, dragPx + dragAmount)
                    }
                },
                onDragCancel = {
                    if (!isRefreshing) dragPx = 0f
                },
                onDragEnd = {
                    if (!isRefreshing && dragPx >= thresholdPx && didTrigger == 0f) {
                        didTrigger = 1f
                        onRefresh()
                    }
                    if (didTrigger == 0f || !isRefreshing) {
                        if (didTrigger == 0f) dragPx = 0f
                    }
                },
            )
        },
        contentAlignment = contentAlignment,
    ) {
        Box(modifier = Modifier.offset { IntOffset(0, dragPx.toInt()) }) {
            content()
        }

        if (isRefreshing) {
            Box(modifier = Modifier.matchParentSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
