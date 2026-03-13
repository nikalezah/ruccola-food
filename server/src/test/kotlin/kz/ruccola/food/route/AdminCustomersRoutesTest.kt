package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.registerCustomer
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

    @Test
    fun adminCanGetCustomers() =
        testApp { client ->
            val adminToken = client.loginAdmin()
            // Create two customers
            client.registerCustomer("cust1@example.com")
            client.registerCustomer("cust2@example.com")

            val response = client.get("/api/customers") { authHeader(adminToken) }
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
            val token = client.registerCustomer("jane@example.com").token
            val response = client.get("/api/customers") { authHeader(token) }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun unauthorizedCasesReturn401() =
        testApp { client ->

            // Missing header
            val noHeaderResp = client.get("/api/customers")
            assertEquals(HttpStatusCode.Unauthorized, noHeaderResp.status)

            // Invalid token format
            val badTokenResp = client.get("/api/customers") { authHeader("not-a-valid-token") }
            assertEquals(HttpStatusCode.Unauthorized, badTokenResp.status)
        }
}
