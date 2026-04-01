package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Plans : IntIdTable("plans") {
    val calories = integer("calories")
    val periodDays = integer("period_days")
    val pricePerDay = integer("price_per_day")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
