package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SquareImagesCarousel200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailsScreen(
    dish: DishDto,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dish.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            val images = dish.images.map { it.url }
            if (images.isNotEmpty()) {
                SquareImagesCarousel200(images)
                Spacer(Modifier.height(16.dp))
            }
            Text(
                dish.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(8.dp))
            Text(dish.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
