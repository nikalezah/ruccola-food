package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.Serializable

@Resource("chats")
class Chats {
    @Resource("my")
    class My(val parent: Chats = Chats())

    @Resource("{chatId}")
    class ChatId(val parent: Chats = Chats(), val chatId: Int) {
        @Resource("messages")
        class Messages(val parent: ChatId, val afterId: Int? = null, val limit: Int? = null)

        @Resource("read")
        class Read(val parent: ChatId)
    }
}

class ChatApi(private val client: HttpClient = httpClient) {
    suspend fun getChats(): List<ChatListItemDto> = client.get(Chats()).body()

    suspend fun getChat(chatId: Int): ChatDto = client.get(Chats.ChatId(chatId = chatId)).body()

    suspend fun getMyChat(): ChatDto = client.get(Chats.My()).body()

    suspend fun getMessages(chatId: Int, afterId: Int? = null, limit: Int? = null): List<MessageDto> =
        client
            .get(Chats.ChatId.Messages(parent = Chats.ChatId(chatId = chatId), afterId = afterId, limit = limit))
            .body()

    suspend fun sendMessage(chatId: Int, body: MessageSendDto): MessageDto {
        val response =
            client.post(Chats.ChatId.Messages(parent = Chats.ChatId(chatId = chatId))) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        if (!response.status.isSuccess()) {
            val msg =
                runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null } ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun markRead(chatId: Int, body: MarkReadDto) {
        val response =
            client.post(Chats.ChatId.Read(parent = Chats.ChatId(chatId = chatId))) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        if (!response.status.isSuccess()) {
            val msg =
                runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null } ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
    }
}

@Serializable
data class ChatListItemDto(
    val id: Int,
    val customerId: Int,
    val customerFirstName: String,
    val customerLastName: String,
    val customerEmail: String,
    @Serializable(with = LocalDateTimeIso8601Serializer::class) val lastMessageAt: LocalDateTime,
    val lastMessageId: Int? = null,
    val lastReadMessageId: Int? = null,
)

@Serializable
data class ChatDto(
    val id: Int,
    val customerId: Int,
    val customerFirstName: String,
    val customerLastName: String,
    val customerEmail: String,
    @Serializable(with = LocalDateTimeIso8601Serializer::class) val lastMessageAt: LocalDateTime,
    val lastMessageId: Int? = null,
    val lastReadMessageId: Int? = null,
)

@Serializable
data class MessageDto(
    val id: Int,
    val chatId: Int,
    val isMine: Boolean,
    val body: String,
    @Serializable(with = LocalDateTimeIso8601Serializer::class) val createdAt: LocalDateTime,
)

@Serializable
data class MessageSendDto(val body: String)

@Serializable
data class MarkReadDto(val lastReadMessageId: Int)
