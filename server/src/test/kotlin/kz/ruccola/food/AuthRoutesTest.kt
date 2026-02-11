package kz.ruccola.food

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        testApplication {
            application { module() }
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"admin@interna.food","password":"admin"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            assertTrue(json.containsKey("token"))
            val user = json["user"]!!.jsonObject
            assertEquals("admin@interna.food", user["email"]!!.jsonPrimitive.content)
            assertEquals("ADMIN", user["role"]!!.jsonPrimitive.content)
        }

    @Test
    fun testLoginFailureWrongPassword() =
        testApplication {
            application { module() }
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"admin@interna.food","password":"wrong"}""")
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun testRegisterAndLoginCustomer() =
        testApplication {
            application { module() }
            val registerResp = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                      "email": "john.doe@example.com",
                      "password": "secret",
                      "confirmPassword": "secret",
                      "firstName": "John",
                      "lastName": "Doe",
                      "address": "123 Main St"
                    }
                    """.trimIndent(),
                )
            }
            assertEquals(HttpStatusCode.Created, registerResp.status)
            val loginResp = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"john.doe@example.com","password":"secret"}""")
            }
            assertEquals(HttpStatusCode.OK, loginResp.status)
            val body = loginResp.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            assertTrue(json.containsKey("token"))
            val user = json["user"]!!.jsonObject
            assertEquals("CUSTOMER", user["role"]!!.jsonPrimitive.content)
        }
}
