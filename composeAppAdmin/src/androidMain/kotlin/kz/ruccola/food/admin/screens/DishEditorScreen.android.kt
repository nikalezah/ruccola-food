package kz.ruccola.food.admin.screens

import androidx.compose.runtime.Composable
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.screens.AndroidDishEditorScreen

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
