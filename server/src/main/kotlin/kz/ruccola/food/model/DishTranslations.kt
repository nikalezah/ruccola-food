package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object DishTranslations : Table("dish_translations") {
    val dishId = reference("dish_id", Dishes, onDelete = ReferenceOption.CASCADE)
    val language = varchar("language", 2)
    val name = varchar("name", 255)
    val description = text("description")
    override val primaryKey = PrimaryKey(dishId, language)
}
