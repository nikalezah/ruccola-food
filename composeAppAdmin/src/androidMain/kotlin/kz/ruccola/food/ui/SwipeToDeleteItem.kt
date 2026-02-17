package kz.ruccola.food.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val threshold = with(LocalDensity.current) { 96.dp.toPx() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        // Background: compact deletes "pill" at both ends (like dish archivation)
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DeletePill()
            DeletePill()
        }
        // Foreground content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(offsetX) > threshold) {
                                onDelete()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onDrag = { _, dragAmount: Offset ->
                            offsetX += dragAmount.x
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun DeletePill() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}
