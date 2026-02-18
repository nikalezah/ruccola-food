package kz.ruccola.food.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

@Composable
actual fun AsyncImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
