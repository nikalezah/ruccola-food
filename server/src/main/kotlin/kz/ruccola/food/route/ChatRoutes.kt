package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.MESSAGE_BODY_MAX_LENGTH
import kz.ruccola.food.api.Chats
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageSendDto
import kz.ruccola.food.api.Role
import kz.ruccola.food.service.ChatService
import kz.ruccola.food.user
import kz.ruccola.food.withRole

fun Route.configureChatRoutes() {
    val chatService = ChatService()

    suspend fun requireChatAccess(call: ApplicationCall, chatId: Int): Boolean {
        val customerId = chatService.getChatCustomerId(chatId)
        if (customerId == null) {
            call.respond(HttpStatusCode.NotFound, "Chat not found")
            return false
        }
        if (call.user.role.isAdmin) return true
        if (customerId != call.user.id) {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
            return false
        }
        return true
    }

    withRole(Role.ADMIN) {
        get<Chats> { call.respond(chatService.getChatsForAdmin(call.user.id)) }

        get<Chats.ChatId> { params ->
            val chat = chatService.getChatById(params.chatId, call.user.id)
            if (chat == null) {
                call.respond(HttpStatusCode.NotFound, "Chat not found")
            } else {
                call.respond(chat)
            }
        }
    }

    withRole(Role.CUSTOMER) { get<Chats.My> { call.respond(chatService.getOrCreateChat(call.user.id, call.user.id)) } }

    get<Chats.ChatId.Messages> { params ->
        if (!requireChatAccess(call, params.parent.chatId)) return@get

        val limit = params.limit?.coerceIn(1, 100) ?: 50
        call.respond(chatService.getMessages(params.parent.chatId, params.afterId, limit, call.user.id))
    }

    post<Chats.ChatId.Messages> { params ->
        if (!requireChatAccess(call, params.parent.chatId)) return@post

        val body = call.receive<MessageSendDto>()
        val cleanedBody = body.body.trim()
        if (cleanedBody.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Message body is required")
            return@post
        }
        if (cleanedBody.length > MESSAGE_BODY_MAX_LENGTH) {
            call.respond(HttpStatusCode.BadRequest, "Message body must be $MESSAGE_BODY_MAX_LENGTH characters or fewer")
            return@post
        }
        val message = chatService.sendMessage(params.parent.chatId, call.user.id, cleanedBody)
        call.respond(HttpStatusCode.Created, message)
    }

    post<Chats.ChatId.Read> { params ->
        if (!requireChatAccess(call, params.parent.chatId)) return@post

        val body = call.receive<MarkReadDto>()
        val updated = chatService.markRead(params.parent.chatId, call.user.id, body.lastReadMessageId)
        if (!updated) {
            call.respond(HttpStatusCode.NotFound, "Message not found")
            return@post
        }
        call.respond(HttpStatusCode.OK)
    }
}
