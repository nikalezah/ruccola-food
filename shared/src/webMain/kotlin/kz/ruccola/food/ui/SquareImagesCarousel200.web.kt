package kz.ruccola.food.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kz.ruccola.food.dishImageUrl

@Composable
actual fun SquareImagesCarousel200(imageUrls: List<String>) {
    if (imageUrls.isEmpty()) return
    HorizontalUncontainedCarousel(
        state = rememberCarouselState { imageUrls.size },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        itemWidth = 200.dp,
        itemSpacing = 8.dp,
    ) { i ->
        AsyncImage(
            model = dishImageUrl(imageUrls[i]),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}
