package kz.ruccola.food.feature.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.ui.ImagePager
import kz.ruccola.food.ui.ResponsiveContainer

@Composable
fun DishDetailsScreen(dish: DishDto, onBack: () -> Unit) {
    // Cap the width so the square image stays a reasonable size in a wide detail pane instead of
    // stretching to the edge.
    ResponsiveContainer(maxContentWidth = 520.dp) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            val images = dish.images.map { it.url }
            if (images.isNotEmpty()) {
                ImagePager(imageUrls = images, onBack = onBack)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(dish.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(8.dp))
                Text(dish.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
