package kz.ruccola.food.route

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.LoginRequestDto
import kz.ruccola.food.api.MarkReadDto
import kz.ruccola.food.api.MessageSendDto
import kz.ruccola.food.api.RegisterRequestDto
import kz.ruccola.food.initializeTestDatabase
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

    private suspend fun loginAdmin(client: HttpClient): String {
        val resp = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto("admin@interna.food", "admin"))
        }
        assertEquals(HttpStatusCode.OK, resp.status)
        val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        return json["token"]!!.jsonPrimitive.content
    }

    private suspend fun registerCustomer(
        client: HttpClient,
        email: String,
    ) {
        val registerResp = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email, "secret", "secret", "John", "Doe", "123 Main St"))
        }
        assertTrue(registerResp.status.isSuccess())
    }

    private suspend fun loginCustomer(
        client: HttpClient,
        email: String,
    ): String {
        val resp = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, "secret"))
        }
        assertEquals(HttpStatusCode.OK, resp.status)
        val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
        return json["token"]!!.jsonPrimitive.content
    }

    @Test
    fun createChatUniquePerCustomer() =
        testApp { client ->
            registerCustomer(client, "cust1@example.com")
            val token = loginCustomer(client, "cust1@example.com")

            val first = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val second = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            val firstJson = Json.parseToJsonElement(first.bodyAsText()).jsonObject
            val secondJson = Json.parseToJsonElement(second.bodyAsText()).jsonObject
            val firstId = firstJson["id"]!!.jsonPrimitive.content.toInt()
            val secondId = secondJson["id"]!!.jsonPrimitive.content.toInt()
            assertEquals(firstId, secondId)
        }

    @Test
    fun sendMessageUpdatesLastMessageAt() =
        testApp { client ->
            registerCustomer(client, "cust2@example.com")
            val token = loginCustomer(client, "cust2@example.com")

            val chatResp = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val chatJson = Json.parseToJsonElement(chatResp.bodyAsText()).jsonObject
            val chatId = chatJson["id"]!!.jsonPrimitive.content.toInt()
            val lastMessageAt = chatJson["lastMessageAt"]!!.jsonPrimitive.content

            val sendResp = client.post("/api/chats/$chatId/messages") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(MessageSendDto("Hello"))
            }
            assertEquals(HttpStatusCode.Created, sendResp.status)

            val updated = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val updatedJson = Json.parseToJsonElement(updated.bodyAsText()).jsonObject
            val updatedLast = updatedJson["lastMessageAt"]!!.jsonPrimitive.content
            assertNotEquals(lastMessageAt, updatedLast)
        }

    @Test
    fun markReadUpdatesMessageReads() =
        testApp { client ->
            registerCustomer(client, "cust3@example.com")
            val token = loginCustomer(client, "cust3@example.com")

            val chatResp = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val chatJson = Json.parseToJsonElement(chatResp.bodyAsText()).jsonObject
            val chatId = chatJson["id"]!!.jsonPrimitive.content.toInt()

            val sendResp = client.post("/api/chats/$chatId/messages") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(MessageSendDto("Read me"))
            }
            val msgJson = Json.parseToJsonElement(sendResp.bodyAsText()).jsonObject
            val messageId = msgJson["id"]!!.jsonPrimitive.content.toInt()

            val markResp = client.post("/api/chats/$chatId/read") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(MarkReadDto(messageId))
            }
            assertEquals(HttpStatusCode.OK, markResp.status)

            val updated = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val updatedJson = Json.parseToJsonElement(updated.bodyAsText()).jsonObject
            val lastReadId = updatedJson["lastReadMessageId"]!!.jsonPrimitive.content.toInt()
            assertEquals(messageId, lastReadId)
        }

    @Test
    fun roleAccessGuards() =
        testApp { client ->
            registerCustomer(client, "cust4@example.com")
            val customerToken = loginCustomer(client, "cust4@example.com")

            val customerList = client.get("/api/chats") {
                header(HttpHeaders.Authorization, "Bearer $customerToken")
            }
            assertEquals(HttpStatusCode.Forbidden, customerList.status)

            val adminToken = loginAdmin(client)
            val adminList = client.get("/api/chats") {
                header(HttpHeaders.Authorization, "Bearer $adminToken")
            }
            assertEquals(HttpStatusCode.OK, adminList.status)
        }

    @Test
    fun customerCannotAccessOtherChat() =
        testApp { client ->
            registerCustomer(client, "cust5@example.com")
            registerCustomer(client, "cust6@example.com")
            val token1 = loginCustomer(client, "cust5@example.com")
            val token2 = loginCustomer(client, "cust6@example.com")

            val chatResp = client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token1")
            }
            val chatId = Json.parseToJsonElement(chatResp.bodyAsText()).jsonObject["id"]!!
                .jsonPrimitive.content.toInt()

            val forbidden = client.get("/api/chats/$chatId/messages") {
                header(HttpHeaders.Authorization, "Bearer $token2")
            }
            assertEquals(HttpStatusCode.Forbidden, forbidden.status)
        }

    @Test
    fun adminSeesAllChats() =
        testApp { client ->
            registerCustomer(client, "cust7@example.com")
            registerCustomer(client, "cust8@example.com")
            val token1 = loginCustomer(client, "cust7@example.com")
            val token2 = loginCustomer(client, "cust8@example.com")

            client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token1")
            }
            client.get("/api/chats/my") {
                header(HttpHeaders.Authorization, "Bearer $token2")
            }

            val adminToken = loginAdmin(client)
            val listResp = client.get("/api/chats") {
                header(HttpHeaders.Authorization, "Bearer $adminToken")
            }
            val arr = Json.parseToJsonElement(listResp.bodyAsText()).jsonArray
            val emails = arr.mapNotNull { it.jsonObject["customerEmail"]?.jsonPrimitive?.content }.toSet()
            assertTrue(emails.contains("cust7@example.com"))
            assertTrue(emails.contains("cust8@example.com"))
        }
}
