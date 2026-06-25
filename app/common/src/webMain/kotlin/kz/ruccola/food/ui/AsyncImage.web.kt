package kz.ruccola.food.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kz.ruccola.food.api.httpClient
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

@Composable
actual fun AsyncImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier,
) {
    var bitmap by remember(model) { mutableStateOf(imageCache[model]) }

    LaunchedEffect(model) {
        if (bitmap != null) return@LaunchedEffect

        try {
            val response = httpClient.get(model)
            val bytes = response.readRawBytes()
            val skiaImage = Image.makeFromEncoded(bytes)
            val result = Bitmap.makeFromImage(skiaImage).asComposeImageBitmap()
            imageCache[model] = result
            bitmap = result
        } catch (e: Exception) {
            println("Error loading image $model: ${e.message}")
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
        )
    } else {
        Spacer(modifier = modifier)
    }
}

private val imageCache = mutableMapOf<String, ImageBitmap>()
