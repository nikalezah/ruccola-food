package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object MessageReads : Table("message_reads") {
    val chatId = reference("chat_id", Chats, onDelete = ReferenceOption.CASCADE).index()
    val userId = reference("user_id", Users).index()
    val lastReadMessageId = reference(
        "last_read_message_id",
        Messages,
        onDelete = ReferenceOption.SET_NULL,
    ).nullable()
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(chatId, userId, name = "pk_message_reads")
}
