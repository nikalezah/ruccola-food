package kz.ruccola.food.admin.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kz.ruccola.food.screens.DayScreen
import kz.ruccola.food.screens.MealPlanDayScreen

@Composable
fun MealPlanDayScreen() {
    var showHistory by remember { mutableStateOf(false) }

    MealPlanDayScreen(
        onHistoryClick = { showHistory = true },
    )

    if (showHistory) {
        DayScreen(onClose = { showHistory = false })
    }
}
