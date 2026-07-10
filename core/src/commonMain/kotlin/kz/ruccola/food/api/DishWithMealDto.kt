package kz.ruccola.food.api

import kotlinx.serialization.Serializable
import kz.ruccola.food.model.Meal

@Serializable
data class DishWithMealDto(val dish: DishDto, val meal: Meal)
