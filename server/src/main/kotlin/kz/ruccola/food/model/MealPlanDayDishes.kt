package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object MealPlanDayDishes : Table("meal_plan_day_dishes") {
    val mealPlanDayId = reference("meal_plan_day_id", MealPlanDays, onDelete = ReferenceOption.CASCADE)
    val dishId = reference("dish_id", Dishes, onDelete = ReferenceOption.CASCADE)
    val meal = varchar("meal", 50)

    override val primaryKey = PrimaryKey(mealPlanDayId, dishId, name = "pk_meal_plan_day_dishes")
}
