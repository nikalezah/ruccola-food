package kz.ruccola.food.screens

import androidx.compose.runtime.Composable
import kz.ruccola.food.api.DishDto

@Composable
expect fun DishEditorScreen(
    initialDish: DishDto?,
    onClose: () -> Unit,
)
