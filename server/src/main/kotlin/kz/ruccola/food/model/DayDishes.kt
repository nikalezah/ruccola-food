package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object DayDishes : Table("day_dishes") {
    val dayId = reference("day_id", Days, onDelete = ReferenceOption.CASCADE)
    val dishId = reference("dish_id", Dishes, onDelete = ReferenceOption.CASCADE)
    val meal = varchar("meal", 50)

    override val primaryKey = PrimaryKey(dayId, dishId, name = "pk_day_dishes")
}
