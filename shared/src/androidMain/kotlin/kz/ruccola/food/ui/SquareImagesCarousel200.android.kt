package kz.ruccola.food.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kz.ruccola.food.dishImageUrl

@OptIn(ExperimentalMaterial3Api::class)
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
                .maskClip(MaterialTheme.shapes.extraLarge)
                .then(Modifier),
        )
    }
}
