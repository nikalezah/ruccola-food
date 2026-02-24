package kz.ruccola.food.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidMealPlanDayScreen(onHistoryClick: () -> Unit) {
    MealPlanDayScreen(onHistoryClick = onHistoryClick)
}
