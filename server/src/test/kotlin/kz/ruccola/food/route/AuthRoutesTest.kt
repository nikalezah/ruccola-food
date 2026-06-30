package kz.ruccola.food.route

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.api.LoginRequestDto
import kz.ruccola.food.api.RegisterRequestDto
import kz.ruccola.food.testApp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest : RouteIntegrationTest() {
    @Test
    fun testAdminLoginSuccess() =
        testApp { client ->
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto("admin@gmail.com", "123qwe"))
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val token = json["token"]!!.jsonPrimitive.content
            assertTrue(token.isNotEmpty())
            assertEquals(token.split(".").size, 3, "Token should be a JWT (3 parts)")
            val user = json["user"]!!.jsonObject
            assertEquals("admin@gmail.com", user["email"]!!.jsonPrimitive.content)
            assertEquals("ADMIN", user["role"]!!.jsonPrimitive.content)
        }

    @Test
    fun testLoginFailureWrongPassword() =
        testApp { client ->
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto("admin@gmail.com", "wrong"))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun testLoginUnknownEmail() =
        testApp { client ->
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto("nobody@example.com", "secret"))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun testRegisterAndLoginCustomer() =
        testApp { client ->
            val registerResp = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequestDto("john.doe@example.com", "secret", "secret", "John", "Doe", "123 Main St"))
            }
            assertEquals(HttpStatusCode.Created, registerResp.status)
            val loginResp = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto("john.doe@example.com", "secret"))
            }
            assertEquals(HttpStatusCode.OK, loginResp.status)
            val body = loginResp.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val token = json["token"]!!.jsonPrimitive.content
            assertTrue(token.isNotEmpty())
            assertEquals(token.split(".").size, 3, "Token should be a JWT (3 parts)")
            val user = json["user"]!!.jsonObject
            assertEquals("CUSTOMER", user["role"]!!.jsonPrimitive.content)
        }

    @Test
    fun testRegisterPasswordMismatch() =
        testApp { client ->
            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequestDto("a@b.com", "one", "two", "John", "Doe", "123 Main St"))
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun testRegisterBlankAddress() =
        testApp { client ->
            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequestDto("a@b.com", "pass", "pass", "John", "Doe", "   "))
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun testRegisterDuplicateEmail() =
        testApp { client ->
            val body = RegisterRequestDto("dup@example.com", "pass", "pass", "John", "Doe", "123 Main St")
            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.apply { assertEquals(HttpStatusCode.Created, status) }
            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.apply { assertEquals(HttpStatusCode.Conflict, status) }
        }
}
