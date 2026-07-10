package kz.ruccola.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SwipeToRemove(
    imageVector: ImageVector,
    iconLabel: String,
    onRemove: () -> Unit,
    shape: Shape = RectangleShape,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    val dismissState = rememberSwipeToDismissBoxState() // todo: adjust positionalThreshold when fixed
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isThresholdReached = dismissState.targetValue != SwipeToDismissBoxValue.Settled
            val containerColor = if (isThresholdReached) colorScheme.errorContainer else colorScheme.surfaceVariant
            val contentColor = if (isThresholdReached) colorScheme.onErrorContainer else colorScheme.onSurfaceVariant

            Box(Modifier.clip(shape).fillMaxSize().background(containerColor)) {
                Row(
                    modifier = Modifier.matchParentSize().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RemoveIcon(imageVector, iconLabel, contentColor)
                    RemoveIcon(imageVector, iconLabel, contentColor)
                }
            }
        },
        onDismiss = { onRemove() },
    ) {
        content()
    }
}

@Composable
private fun RemoveIcon(imageVector: ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector, contentDescription = null, tint = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
