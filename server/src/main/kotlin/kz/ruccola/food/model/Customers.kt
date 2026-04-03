package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object Customers : IdTable<Int>("customers") {
    override val id: Column<EntityID<Int>> = reference("id", Users)
    override val primaryKey = PrimaryKey(id)
    val address = text("address")
    val needsCutlery = bool("needs_cutlery")
    val weekendDelivery = bool("weekend_delivery")
    val morningDelivery = bool("morning_delivery")
}
