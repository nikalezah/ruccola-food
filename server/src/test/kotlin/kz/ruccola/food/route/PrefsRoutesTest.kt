package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.CustomerPlanWithPrefsDto
import kz.ruccola.food.api.CustomerPrefsDto
import kz.ruccola.food.api.CustomerPrefsUpdateDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.model.Plans
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrefsRoutesTest : RouteIntegrationTest() {
    @Test
    fun testGetPrefsNoPlan() = testApp { client ->
        val customer = client.registerCustomer("customer1@ruccola.food")
        val response = client.get("/api/customers/plan") { authHeader(customer.token) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetPrefsWithPlan() = testApp { client ->
        val customer = client.registerCustomer("customer1@ruccola.food")
        val planId = suspendTransaction {
            Plans.insertAndGetId {
                    it[calories] = 1200
                    it[periodDays] = 7
                    it[pricePerDay] = 2000
            }
                .value
        }
        client
            .post("/api/customers/plan") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPlanCreateDto(planId = planId, days = 7, chosenDate = LocalDate(2026, 4, 6)))
            }
            .apply { assertEquals(HttpStatusCode.Created, status) }
        val response = client.get("/api/customers/plan") { authHeader(customer.token) }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<CustomerPlanWithPrefsDto>()
        assertFalse(body.prefs.needsCutlery)
        assertFalse(body.prefs.weekendDelivery)
        assertFalse(body.prefs.morningDelivery)
    }

    @Test
    fun testSaveAllPrefs() = testApp { client ->
        val customer = client.registerCustomer("customer1@ruccola.food")
        val response =
            client.put("/api/customers/prefs") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(needsCutlery = true, weekendDelivery = true, morningDelivery = true))
            }
        assertEquals(HttpStatusCode.OK, response.status)
        val prefs = response.body<CustomerPrefsDto>()
        assertTrue(prefs.needsCutlery)
        assertTrue(prefs.weekendDelivery)
        assertTrue(prefs.morningDelivery)
    }

    @Test
    fun testSaveSinglePref() = testApp { client ->
        val customer = client.registerCustomer("customer1@ruccola.food")

        client
            .put("/api/customers/prefs") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(needsCutlery = true))
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }

        client
            .put("/api/customers/prefs") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(morningDelivery = true))
            }
            .apply {
                assertEquals(HttpStatusCode.OK, status)
                val prefs = body<CustomerPrefsDto>()
                assertTrue(prefs.needsCutlery)
                assertFalse(prefs.weekendDelivery)
                assertTrue(prefs.morningDelivery)
            }
    }

    @Test
    fun testPrefsUnauthorized() = testApp { client ->
        client
            .put("/api/customers/prefs") {
                contentType(ContentType.Application.Json)
                setBody(CustomerPrefsUpdateDto(needsCutlery = true))
            }
            .apply { assertEquals(HttpStatusCode.Unauthorized, status) }
    }
}
