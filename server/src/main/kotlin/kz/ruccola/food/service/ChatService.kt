package kz.ruccola.food.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.ChatDto
import kz.ruccola.food.api.ChatListItemDto
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.Chats
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.MessageReads
import kz.ruccola.food.model.Messages
import kz.ruccola.food.model.Users
import kz.ruccola.food.now
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

class ChatService {
    suspend fun getOrCreateChat(
        customerId: Int,
        viewerUserId: Int,
    ): ChatDto =
        dbQuery {
            val existing = Chats.selectAll().where { Chats.customerId eq customerId }.singleOrNull()
            val chatId = if (existing == null) {
                Chats.insertAndGetId {
                    it[Chats.customerId] = customerId
                    it[Chats.createdAt] = now()
                    it[Chats.lastMessageAt] = now()
                }.value
            } else {
                existing[Chats.id].value
            }
            toChatDto(chatId, viewerUserId) ?: error("Chat not found after create")
        }

    suspend fun getChatById(
        chatId: Int,
        viewerUserId: Int,
    ): ChatDto? =
        dbQuery {
            toChatDto(chatId, viewerUserId)
        }

    suspend fun getChatCustomerId(chatId: Int): Int? =
        dbQuery {
            Chats.select(Chats.customerId).where { Chats.id eq chatId }
                .singleOrNull()
                ?.get(Chats.customerId)
                ?.value
        }

    suspend fun getChatsForAdmin(adminUserId: Int): List<ChatListItemDto> =
        dbQuery {
            Chats.innerJoin(Customers).innerJoin(Users).selectAll()
                .orderBy(Chats.lastMessageAt to SortOrder.DESC)
                .map { row ->
                    val chatId = row[Chats.id].value
                    ChatListItemDto(
                        id = chatId,
                        customerId = row[Customers.id].value,
                        customerFirstName = row[Users.firstName],
                        customerLastName = row[Users.lastName],
                        customerEmail = row[Users.email],
                        lastMessageAt = row[Chats.lastMessageAt],
                        lastMessageId = getLastMessageId(chatId),
                        lastReadMessageId = getLastReadMessageId(chatId, adminUserId),
                    )
                }
                .toList()
        }

    suspend fun getMessages(
        chatId: Int,
        afterId: Int?,
        limit: Int,
    ): List<MessageDto> =
        dbQuery {
            val condition =
                if (afterId == null) {
                    Messages.chatId eq chatId
                } else {
                    (Messages.chatId eq chatId) and (Messages.id greater afterId)
                }
            Messages.selectAll()
                .where { condition }
                .orderBy(Messages.id to SortOrder.ASC)
                .limit(limit)
                .map(::toMessageDto)
                .toList()
        }

    suspend fun sendMessage(
        chatId: Int,
        senderId: Int,
        body: String,
    ): MessageDto =
        dbQuery {
            val message = Messages.insertReturning {
                it[Messages.chatId] = chatId
                it[Messages.senderUserId] = senderId
                it[Messages.body] = body
                it[Messages.createdAt] = now()
            }.single()
            Chats.update({ Chats.id eq chatId }) {
                it[lastMessageAt] = now()
            }
            toMessageDto(message)
        }

    suspend fun markRead(
        chatId: Int,
        userId: Int,
        lastReadMessageId: Int,
    ): Boolean =
        dbQuery {
            val messageExists = Messages.select(Messages.id)
                .where { (Messages.id eq lastReadMessageId) and (Messages.chatId eq chatId) }
                .singleOrNull()
                ?.get(Messages.id)
                ?.value
                ?: return@dbQuery false

            val existing = MessageReads.selectAll()
                .where { (MessageReads.chatId eq chatId) and (MessageReads.userId eq userId) }
                .singleOrNull()

            if (existing == null) {
                MessageReads.insert {
                    it[MessageReads.chatId] = chatId
                    it[MessageReads.userId] = userId
                    it[MessageReads.lastReadMessageId] = messageExists
                    it[MessageReads.updatedAt] = now()
                }
            } else {
                MessageReads.update(
                    where = { (MessageReads.chatId eq chatId) and (MessageReads.userId eq userId) },
                ) {
                    it[MessageReads.lastReadMessageId] = messageExists
                    it[MessageReads.updatedAt] = now()
                }
            }
            true
        }

    private suspend fun getLastMessageId(chatId: Int): Int? =
        Messages.select(Messages.id)
            .where { Messages.chatId eq chatId }
            .orderBy(Messages.id to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.get(Messages.id)
            ?.value

    private suspend fun getLastReadMessageId(
        chatId: Int,
        userId: Int,
    ): Int? =
        MessageReads.select(MessageReads.lastReadMessageId)
            .where { (MessageReads.chatId eq chatId) and (MessageReads.userId eq userId) }
            .singleOrNull()
            ?.get(MessageReads.lastReadMessageId)
            ?.value

    private suspend fun toChatDto(
        chatId: Int,
        viewerUserId: Int,
    ): ChatDto? {
        val row = Chats.innerJoin(Customers).innerJoin(Users).selectAll()
            .where { Chats.id eq chatId }
            .singleOrNull()
            ?: return null

        return ChatDto(
            id = row[Chats.id].value,
            customerId = row[Customers.id].value,
            customerFirstName = row[Users.firstName],
            customerLastName = row[Users.lastName],
            customerEmail = row[Users.email],
            lastMessageAt = row[Chats.lastMessageAt],
            lastMessageId = getLastMessageId(chatId),
            lastReadMessageId = getLastReadMessageId(chatId, viewerUserId),
        )
    }

    private fun toMessageDto(row: org.jetbrains.exposed.v1.core.ResultRow): MessageDto =
        MessageDto(
            id = row[Messages.id].value,
            chatId = row[Messages.chatId].value,
            senderUserId = row[Messages.senderUserId].value,
            body = row[Messages.body],
            createdAt = row[Messages.createdAt],
        )
}
