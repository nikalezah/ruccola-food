package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Chats : IntIdTable("chats") {
    val customerId = reference("customer_id", Customers).uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val lastMessageAt = datetime("last_message_at").defaultExpression(CurrentDateTime)
}
