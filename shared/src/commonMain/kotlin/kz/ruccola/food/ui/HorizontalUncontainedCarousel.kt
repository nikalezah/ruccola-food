package kz.ruccola.food.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class CarouselState internal constructor(
    val itemCount: Int,
)

@Composable
fun rememberCarouselState(itemCount: () -> Int): CarouselState {
    val count = itemCount()
    return remember(count) { CarouselState(count) }
}

@Composable
fun HorizontalUncontainedCarousel(
    state: CarouselState,
    modifier: Modifier = Modifier,
    itemWidth: Dp,
    itemSpacing: Dp,
    content: @Composable (index: Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    BoxWithConstraints(modifier = modifier) {
        val totalWidth = itemWidth * state.itemCount +
            itemSpacing * (state.itemCount - 1).coerceAtLeast(0)
        val sidePadding = if (totalWidth < maxWidth) (maxWidth - totalWidth) / 2 else 0.dp

        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = sidePadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            repeat(state.itemCount) { index ->
                Box(modifier = Modifier.width(itemWidth)) {
                    content(index)
                }
            }
        }
    }
}
