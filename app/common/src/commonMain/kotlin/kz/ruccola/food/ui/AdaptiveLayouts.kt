package kz.ruccola.food.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Caps the width of its [content] to [maxContentWidth] so that "feed" / form style screens don't
 * stretch edge-to-edge on wide windows (a key Material 3 readability guideline). The content is
 * aligned to the start, so it stays visually attached to the side navigation rail instead of floating
 * in the middle with a large empty gap. On compact widths the cap has no effect, so the content
 * simply fills the available width.
 *
 * Wrap a whole screen `Scaffold` with this (rather than just its inner content) so that the top bar,
 * floating action button, and other controls stay next to the content at the same width.
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 720.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Box(Modifier.widthIn(max = maxContentWidth).fillMaxSize()) {
            content()
        }
    }
}

/**
 * Default placeholder for an empty list-detail detail pane on wide windows.
 */
@Composable
fun EmptyDetailPane(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
