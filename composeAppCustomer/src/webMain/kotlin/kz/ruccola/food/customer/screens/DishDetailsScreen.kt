package kz.ruccola.food.customer.screens

import androidx.compose.foundation.layout.Box
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
import kz.ruccola.food.customer.LocalStrings
import kz.ruccola.food.web.common.ui.SquareImagesCarousel200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailsScreen(
    dishId: Int,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var dish by remember { mutableStateOf<DishDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dishId) {
        scope.launch {
            try {
                dish = DishApi().getDishById(dishId)
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dish?.name ?: strings.loading) },
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
                    Text(text = strings.errorPrefix.replace("%s", error!!), color = MaterialTheme.colorScheme.error)
                }
            }

            dish == null -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    CircularProgressIndicator()
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
                    val images = d.images.map { it.url }
                    SquareImagesCarousel200(images)
                    if (images.isNotEmpty()) {
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
