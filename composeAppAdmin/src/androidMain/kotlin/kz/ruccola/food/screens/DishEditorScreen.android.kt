package kz.ruccola.food.screens

import androidx.compose.runtime.Composable
import kz.ruccola.food.api.DishDto

@Composable
actual fun DishEditorScreen(
    initialDish: DishDto?,
    onClose: () -> Unit,
) {
    AndroidDishEditorScreen(
        initialDish = initialDish,
        onClose = onClose,
    )
}
