package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object MealPlanDays : IntIdTable("meal_plan_days") {
    val serial = integer("serial").uniqueIndex()
    val current = bool("current").default(false)
}
