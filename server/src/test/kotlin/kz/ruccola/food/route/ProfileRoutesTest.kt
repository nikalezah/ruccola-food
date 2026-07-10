package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.CustomerUpdateDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRoutesTest : RouteIntegrationTest() {
    @Test
    fun testGetProfileWithValidToken() = testApp { client ->
        val email = "customer1@ruccola.food"
        val customer = client.registerCustomer(email)
        client
            .get("/api/customers/profile") { authHeader(customer.token) }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                val json = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(email, json["email"]!!.jsonPrimitive.content)
                assertEquals("CUSTOMER", json["role"]!!.jsonPrimitive.content)
            }
    }

    @Test
    fun testGetProfileUnauthorized() = testApp { client ->
        client.get("/api/customers/profile").apply { assertEquals(HttpStatusCode.Unauthorized, status) }

        client
            .get("/api/customers/profile") { authHeader("invalid-token") }
            .apply { assertEquals(HttpStatusCode.Unauthorized, status) }
    }

    @Test
    fun testLogoutEndpoint() = testApp { client ->
        client.post("/api/auth/logout").apply { assertEquals(HttpStatusCode.OK, status) }
    }

    @Test
    fun testUpdateProfile() = testApp { client ->
        val customer = client.registerCustomer()
        client
            .put("/api/customers/profile") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerUpdateDto(firstName = "Jane", lastName = "Smith", address = "456 Oak Ave"))
            }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                val updated = body<CustomerDto>()
                assertEquals("Jane", updated.firstName)
                assertEquals("Smith", updated.lastName)
                assertEquals("456 Oak Ave", updated.address)
            }
    }

    @Test
    fun testUpdateProfilePartialPreservesOtherFields() = testApp { client ->
        val customer = client.registerCustomer()
        client.put("/api/customers/profile") {
            authHeader(customer.token)
            contentType(ContentType.Application.Json)
            setBody(CustomerUpdateDto(firstName = "Jane", lastName = "Smith", address = "456 Oak Ave"))
        }
        client
            .put("/api/customers/profile") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerUpdateDto(lastName = "Updated"))
            }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                val updated = body<CustomerDto>()
                assertEquals("Jane", updated.firstName)
                assertEquals("Updated", updated.lastName)
                assertEquals("456 Oak Ave", updated.address)
            }
    }
}
