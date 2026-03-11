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
import kz.ruccola.food.api.LoginRequestDto
import kz.ruccola.food.api.RegisterRequestDto
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.testApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testAdminLoginSuccess() =
        testApp { client ->
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto("admin@ruccola.food", "admin"))
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val token = json["token"]!!.jsonPrimitive.content
            assertTrue(token.isNotEmpty())
            assertTrue(token.split(".").size == 3, "Token should be a JWT (3 parts)")
            val user = json["user"]!!.jsonObject
            assertEquals("admin@ruccola.food", user["email"]!!.jsonPrimitive.content)
            assertEquals("ADMIN", user["role"]!!.jsonPrimitive.content)
        }

    @Test
    fun testLoginFailureWrongPassword() =
        testApp { client ->
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto("admin@ruccola.food", "wrong"))
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
            assertTrue(token.split(".").size == 3, "Token should be a JWT (3 parts)")
            val user = json["user"]!!.jsonObject
            assertEquals("CUSTOMER", user["role"]!!.jsonPrimitive.content)
        }
}
