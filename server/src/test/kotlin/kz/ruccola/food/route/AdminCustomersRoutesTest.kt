package kz.ruccola.food.route

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
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

class AdminCustomersRoutesTest {
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
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, "secret"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val json = response.body<JsonObject>()
        return json["token"]!!.jsonPrimitive.content
    }

    @Test
    fun adminCanGetCustomers() =
        testApp { client ->

            val adminToken = loginAdmin(client)
            // Create two customers
            registerCustomer(client, "cust1@example.com")
            registerCustomer(client, "cust2@example.com")

            val response = client.get("/api/customers") {
                header(HttpHeaders.Authorization, "Bearer $adminToken")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val arr = response.body<JsonArray>()
            // Should contain at least the two created customers
            val emails = arr.mapNotNull { it.jsonObject["email"]?.jsonPrimitive?.content }.toSet()
            assertTrue(emails.contains("cust1@example.com"))
            assertTrue(emails.contains("cust2@example.com"))
            // All returned users must be role CUSTOMER
            arr.forEach { el ->
                assertEquals("CUSTOMER", el.jsonObject["role"]!!.jsonPrimitive.content)
            }
        }

    @Test
    fun customerForbiddenToGetCustomers() =
        testApp { client ->

            // Register and login as customer
            registerCustomer(client, "jane@example.com")
            val token = loginCustomer(client, "jane@example.com")

            val response = client.get("/api/customers") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun unauthorizedCasesReturn401() =
        testApp { client ->

            // Missing header
            val noHeaderResp = client.get("/api/customers")
            assertEquals(HttpStatusCode.Unauthorized, noHeaderResp.status)

            // Invalid token format
            val badTokenResp = client.get("/api/customers") {
                header(HttpHeaders.Authorization, "Bearer not-a-valid-token")
            }
            assertEquals(HttpStatusCode.Unauthorized, badTokenResp.status)
        }
}
