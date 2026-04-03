package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.CustomerPrefsDto
import kz.ruccola.food.api.CustomerPrefsUpdateDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrefsRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testGetProfileIncludesPrefs() =
        testApp { client ->
            val customer = client.registerCustomer("customer1@ruccola.food")
            val response = client.get("/api/customers/profile") { authHeader(customer.token) }
            assertEquals(HttpStatusCode.OK, response.status)
            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject["prefs"]!!.jsonObject
            assertFalse(json["needsCutlery"]!!.jsonPrimitive.content.toBoolean())
            assertFalse(json["weekendDelivery"]!!.jsonPrimitive.content.toBoolean())
            assertFalse(json["morningDelivery"]!!.jsonPrimitive.content.toBoolean())
        }

    @Test
    fun testSaveAllPrefs() =
        testApp { client ->
            val customer = client.registerCustomer("customer1@ruccola.food")
            val response = client.put("/api/customers/prefs") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(
                    CustomerPrefsUpdateDto(
                        needsCutlery = true,
                        weekendDelivery = true,
                        morningDelivery = true,
                    ),
                )
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val prefs = response.body<CustomerPrefsDto>()
            assertTrue(prefs.needsCutlery)
            assertTrue(prefs.weekendDelivery)
            assertTrue(prefs.morningDelivery)
        }

    @Test
    fun testSaveSinglePref() =
        testApp { client ->
            val customer = client.registerCustomer("customer1@ruccola.food")

            client.put("/api/customers/prefs") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(needsCutlery = true))
            }.apply { assertEquals(HttpStatusCode.OK, status) }

            client.put("/api/customers/prefs") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(morningDelivery = true))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val prefs = body<CustomerPrefsDto>()
                assertTrue(prefs.needsCutlery)
                assertFalse(prefs.weekendDelivery)
                assertTrue(prefs.morningDelivery)
            }
        }

    @Test
    fun testPrefsUnauthorized() =
        testApp { client ->
            client.put("/api/customers/prefs") {
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(needsCutlery = true))
            }.apply { assertEquals(HttpStatusCode.Unauthorized, status) }
        }
}
