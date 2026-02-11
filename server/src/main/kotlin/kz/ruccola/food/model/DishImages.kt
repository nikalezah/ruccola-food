package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object DishImages : IntIdTable("dish_images") {
    val dishId = reference("dish_id", Dishes)
    val fileId = reference("file_id", Files, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
