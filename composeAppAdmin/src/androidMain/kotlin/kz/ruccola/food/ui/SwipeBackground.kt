package kz.ruccola.food.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SwipeBackground(
    imageVector: ImageVector,
    contentDescription: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}
