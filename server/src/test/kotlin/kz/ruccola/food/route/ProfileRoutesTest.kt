package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testGetProfileWithValidToken() =
        testApp { client ->
            val customer = client.registerCustomer("customer1@ruccola.food")
            client.get("/api/customers/profile") { authHeader(customer.token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val json = Json.parseToJsonElement(bodyAsText()).jsonObject
                    assertEquals("customer1@ruccola.food", json["email"]!!.jsonPrimitive.content)
                    assertEquals("CUSTOMER", json["role"]!!.jsonPrimitive.content)
                }
        }

    @Test
    fun testGetProfileUnauthorized() =
        testApp { client ->
            client.get("/api/customers/profile")
                .apply { assertEquals(HttpStatusCode.Unauthorized, status) }

            client.get("/api/customers/profile") { authHeader("invalid-token") }
                .apply { assertEquals(HttpStatusCode.Unauthorized, status) }
        }

    @Test
    fun testLogoutEndpoint() =
        testApp { client ->
            client.post("/api/auth/logout")
                .apply { assertEquals(HttpStatusCode.OK, status) }
        }
}
