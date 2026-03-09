package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.screen_dish_details_title
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SquareImagesCarousel200
import kz.ruccola.food.viewmodel.DishViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailsScreen(
    dishId: Int,
    onBack: () -> Unit,
    viewModel: DishViewModel = viewModel { DishViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(dishId) {
        viewModel.loadDish(dishId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.dish?.name ?: stringResource(Res.string.screen_dish_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val error = state.error
        val dish = state.dish
        val isLoading = state.isLoading

        when {
            error != null -> {
                Column(Modifier.padding(padding).padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.error_prefix, error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            isLoading && dish == null -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            dish != null -> {
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
    }
}
