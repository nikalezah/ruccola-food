package kz.ruccola.food.model

import kz.ruccola.food.api.Role
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Users : IntIdTable("users") {
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100) // In a real app, this would be hashed
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val role = enumerationByName("role", 20, Role::class)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
