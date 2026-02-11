package kz.ruccola.food.model

import kz.ruccola.food.MESSAGE_BODY_MAX_LENGTH
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Messages : IntIdTable("messages") {
    val chatId = reference("chat_id", Chats, onDelete = ReferenceOption.CASCADE).index()
    val senderUserId = reference("sender_user_id", Users).index()
    val body = varchar("body", MESSAGE_BODY_MAX_LENGTH)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
