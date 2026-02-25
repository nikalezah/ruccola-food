package kz.ruccola.food.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.ui.SquareImagesCarousel200

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun AndroidDishDetailsScreen(
    dishId: Int,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var dish by remember { mutableStateOf<DishDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dishId) {
        scope.launch {
            runCatching { DishApi().getDishById(dishId) }
                .onSuccess { dish = it }
                .onFailure { error = it.message ?: it.toString() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dish details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            error != null -> {
                Column(Modifier.padding(padding).padding(16.dp)) {
                    Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                }
            }

            dish == null -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                ) {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                }
            }

            else -> {
                val d = dish!!
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    val images = d.images
                    if (images.isNotEmpty()) {
                        SquareImagesCarousel200(images.map { it.url })
                        Spacer(Modifier.height(16.dp))
                    }
                    Text(d.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(8.dp))
                    Text(d.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
