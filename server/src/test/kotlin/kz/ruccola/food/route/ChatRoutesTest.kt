package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageSendDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ChatRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun createChatUniquePerCustomer() =
        testApp { client ->
            val token = client.registerCustomer("cust1@example.com").token

            val first = client.get("/api/chats/my") { authHeader(token) }
            val second = client.get("/api/chats/my") { authHeader(token) }

            val firstJson = Json.parseToJsonElement(first.bodyAsText()).jsonObject
            val secondJson = Json.parseToJsonElement(second.bodyAsText()).jsonObject
            val firstId = firstJson["id"]!!.jsonPrimitive.content.toInt()
            val secondId = secondJson["id"]!!.jsonPrimitive.content.toInt()
            assertEquals(firstId, secondId)
        }

    @Test
    fun sendMessageUpdatesLastMessageAt() =
        testApp { client ->
            val token = client.registerCustomer("cust2@example.com").token

            val chatResp = client.get("/api/chats/my") { authHeader(token) }
            val chatJson = Json.parseToJsonElement(chatResp.bodyAsText()).jsonObject
            val chatId = chatJson["id"]!!.jsonPrimitive.content.toInt()
            val lastMessageAt = chatJson["lastMessageAt"]!!.jsonPrimitive.content

            val sendResp = client.post("/api/chats/$chatId/messages") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MessageSendDto("Hello"))
            }
            assertEquals(HttpStatusCode.Created, sendResp.status)

            val updated = client.get("/api/chats/my") { authHeader(token) }
            val updatedJson = Json.parseToJsonElement(updated.bodyAsText()).jsonObject
            val updatedLast = updatedJson["lastMessageAt"]!!.jsonPrimitive.content
            assertNotEquals(lastMessageAt, updatedLast)
        }

    @Test
    fun markReadUpdatesMessageReads() =
        testApp { client ->
            val token = client.registerCustomer("cust3@example.com").token

            val chatResp = client.get("/api/chats/my") { authHeader(token) }
            val chatJson = Json.parseToJsonElement(chatResp.bodyAsText()).jsonObject
            val chatId = chatJson["id"]!!.jsonPrimitive.content.toInt()

            val sendResp = client.post("/api/chats/$chatId/messages") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MessageSendDto("Read me"))
            }
            val msgJson = Json.parseToJsonElement(sendResp.bodyAsText()).jsonObject
            val messageId = msgJson["id"]!!.jsonPrimitive.content.toInt()

            val markResp = client.post("/api/chats/$chatId/read") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MarkReadDto(messageId))
            }
            assertEquals(HttpStatusCode.OK, markResp.status)

            val updated = client.get("/api/chats/my") { authHeader(token) }
            val updatedJson = Json.parseToJsonElement(updated.bodyAsText()).jsonObject
            val lastReadId = updatedJson["lastReadMessageId"]!!.jsonPrimitive.content.toInt()
            assertEquals(messageId, lastReadId)
        }

    @Test
    fun roleAccessGuards() =
        testApp { client ->
            val customerToken = client.registerCustomer("cust4@example.com").token

            val customerList = client.get("/api/chats") { authHeader(customerToken) }
            assertEquals(HttpStatusCode.Forbidden, customerList.status)

            val adminToken = client.loginAdmin()
            val adminList = client.get("/api/chats") { authHeader(adminToken) }
            assertEquals(HttpStatusCode.OK, adminList.status)
        }

    @Test
    fun customerCannotAccessOtherChat() =
        testApp { client ->
            val token1 = client.registerCustomer("cust5@example.com").token
            val token2 = client.registerCustomer("cust6@example.com").token

            val chatResp = client.get("/api/chats/my") { authHeader(token1) }
            val chatId = Json.parseToJsonElement(chatResp.bodyAsText()).jsonObject["id"]!!
                .jsonPrimitive.content.toInt()

            val forbidden = client.get("/api/chats/$chatId/messages") { authHeader(token2) }
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)
        }

    @Test
    fun adminSeesAllChats() =
        testApp { client ->
            val token1 = client.registerCustomer("cust7@example.com").token
            val token2 = client.registerCustomer("cust8@example.com").token

            client.get("/api/chats/my") { authHeader(token1) }
            client.get("/api/chats/my") { authHeader(token2) }

            val adminToken = client.loginAdmin()
            val listResp = client.get("/api/chats") { authHeader(adminToken) }
            val arr = Json.parseToJsonElement(listResp.bodyAsText()).jsonArray
            val emails = arr.mapNotNull { it.jsonObject["customerEmail"]?.jsonPrimitive?.content }.toSet()
            assertTrue(emails.contains("cust7@example.com"))
            assertTrue(emails.contains("cust8@example.com"))
        }
}
