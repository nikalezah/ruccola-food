package kz.ruccola.food.route

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.MESSAGE_BODY_MAX_LENGTH
import kz.ruccola.food.api.Chats
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageSendDto
import kz.ruccola.food.service.ChatService
import kz.ruccola.food.service.UserService

private data class AuthUser(
    val id: Int,
    val isAdmin: Boolean,
)

fun Route.configureChatRoutes() {
    val chatService = ChatService()
    val userService = UserService()

    suspend fun requireAuth(call: io.ktor.server.application.ApplicationCall): AuthUser? {
        val authHeader = call.request.headers[HttpHeaders.Authorization]
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return null
        }
        val token = authHeader.removePrefix("Bearer ").trim()
        val parts = token.split("-")
        val userId = parts.lastOrNull()?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            return null
        }
        val isAdmin = userService.isAdmin(userId)
        if (isAdmin == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return null
        }
        return AuthUser(id = userId, isAdmin = isAdmin)
    }

    suspend fun requireChatAccess(
        call: io.ktor.server.application.ApplicationCall,
        chatId: Int,
        user: AuthUser,
    ): Boolean {
        val customerId = chatService.getChatCustomerId(chatId)
        if (customerId == null) {
            call.respond(HttpStatusCode.NotFound, "Chat not found")
            return false
        }
        if (user.isAdmin) return true
        if (customerId != user.id) {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
            return false
        }
        return true
    }

    get<Chats> {
        val user = requireAuth(call) ?: return@get
        if (!user.isAdmin) {
            call.respond(HttpStatusCode.Forbidden, "Admin access required")
            return@get
        }
        call.respond(chatService.getChatsForAdmin(user.id))
    }

    get<Chats.ChatId> { params ->
        val user = requireAuth(call) ?: return@get
        if (!user.isAdmin) {
            call.respond(HttpStatusCode.Forbidden, "Admin access required")
            return@get
        }
        val chat = chatService.getChatById(params.chatId, user.id)
        if (chat == null) {
            call.respond(HttpStatusCode.NotFound, "Chat not found")
        } else {
            call.respond(chat)
        }
    }

    get<Chats.My> {
        val user = requireAuth(call) ?: return@get
        if (user.isAdmin) {
            call.respond(HttpStatusCode.Forbidden, "Customer access required")
            return@get
        }
        call.respond(chatService.getOrCreateChat(user.id, user.id))
    }

    get<Chats.ChatId.Messages> { params ->
        val user = requireAuth(call) ?: return@get
        if (!requireChatAccess(call, params.parent.chatId, user)) return@get

        val limit = params.limit?.coerceIn(1, 100) ?: 50
        call.respond(chatService.getMessages(params.parent.chatId, params.afterId, limit))
    }

    post<Chats.ChatId.Messages> { params ->
        val user = requireAuth(call) ?: return@post
        if (!requireChatAccess(call, params.parent.chatId, user)) return@post

        val body = call.receive<MessageSendDto>()
        val cleanedBody = body.body.trim()
        if (cleanedBody.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Message body is required")
            return@post
        }
        if (cleanedBody.length > MESSAGE_BODY_MAX_LENGTH) {
            call.respond(
                HttpStatusCode.BadRequest,
                "Message body must be $MESSAGE_BODY_MAX_LENGTH characters or fewer",
            )
            return@post
        }
        val message = chatService.sendMessage(params.parent.chatId, user.id, cleanedBody)
        call.respond(HttpStatusCode.Created, message)
    }

    post<Chats.ChatId.Read> { params ->
        val user = requireAuth(call) ?: return@post
        if (!requireChatAccess(call, params.parent.chatId, user)) return@post

        val body = call.receive<MarkReadDto>()
        val updated = chatService.markRead(params.parent.chatId, user.id, body.lastReadMessageId)
        if (!updated) {
            call.respond(HttpStatusCode.NotFound, "Message not found")
            return@post
        }
        call.respond(HttpStatusCode.OK)
    }
}
