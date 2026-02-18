package kz.ruccola.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun Badge(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    content: (@Composable () -> Unit)? = null,
) {
    val hasContent = content != null
    val minSize = if (hasContent) 16.dp else 8.dp
    val padding = if (hasContent) 4.dp else 0.dp

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .background(containerColor, CircleShape)
            .padding(horizontal = padding),
        contentAlignment = Alignment.Center,
    ) {
        if (content != null) {
            ProvideTextStyle(MaterialTheme.typography.labelSmall.copy(color = contentColor)) {
                content()
            }
        }
    }
}

@Composable
fun BadgedBox(
    badge: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    badgeOffset: DpOffset = DpOffset(6.dp, (-6).dp),
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier.align(Alignment.TopEnd)
                .offset(x = badgeOffset.x, y = badgeOffset.y),
        ) {
            badge()
        }
    }
}
