package kz.ruccola.food.route

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.CustomerPlans
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.model.Plans
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import kz.ruccola.food.today
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testCrud() =
        testApp { client ->
            // Create
            var id = 0
            val token = client.loginAdmin()
            client.post("/api/plans") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(PlanCreateDto(PlanCalories.C1800, PlanDays.D30, 2000))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                id = obj["id"]!!.jsonPrimitive.int
                assertEquals("C1800", obj["calories"]!!.jsonPrimitive.content)
            }

            // List
            client.get("/api/plans") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    assertTrue(arr.isNotEmpty())
                }

            // Update
            client.put("/api/plans/$id") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(PlanUpdateDto(PlanCalories.C2000))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("C2000", obj["calories"]!!.jsonPrimitive.content)
            }

            // Delete
            client.delete("/api/plans/$id") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }
            client.get("/api/plans/$id") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.NotFound, status) }
        }

    @Test
    fun testCustomerPlanSelection() =
        testApp { client ->
            // Setup: Create a customer and plans
            val customer = client.registerCustomer()
            var planId1800 = 0
            var planId2000 = 0

            suspendTransaction {
                // Create plans with different options
                val planId1 = Plans.insertAndGetId {
                    it[calories] = 1800
                    it[periodDays] = 30
                    it[pricePerDay] = 2000
                }.value
                planId1800 = planId1

                val planId2 = Plans.insertAndGetId {
                    it[calories] = 2000
                    it[periodDays] = 14
                    it[pricePerDay] = 2500
                }.value
                planId2000 = planId2

                // Additional plans for option testing
                Plans.insert {
                    it[calories] = 1800
                    it[periodDays] = 14
                    it[pricePerDay] = 1800
                }
                Plans.insert {
                    it[calories] = 2200
                    it[periodDays] = 30
                    it[pricePerDay] = 2200
                }
            }

            // Test 1: Get a customer plan when none exists
            client.get("/api/customers/plan") { authHeader(customer.token) }
                .apply {
                    assertEquals(HttpStatusCode.NotFound, status)
                }

            // Test 2: Save customer plan
            val today = today()
            client.post("/api/customers/plan") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPlanCreateDto(planId1800, today))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(customer.user.id, obj["customerId"]!!.jsonPrimitive.int)
                assertEquals(planId1800, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(today.toString(), obj["chosenDate"]!!.jsonPrimitive.content)
            }

            // Test 3: Get a customer plan after saving
            client.get("/api/customers/plan") { authHeader(customer.token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                    assertEquals(customer.user.id, obj["customerId"]!!.jsonPrimitive.int)
                    assertEquals(planId1800, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                }

            // Test 4: Update plan with the same date (should replace)
            client.post("/api/customers/plan") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPlanCreateDto(planId2000, today))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(planId2000, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
            }

            // Verify only one record exists for today
            val recordCount = suspendTransaction {
                CustomerPlans.selectAll().where { CustomerPlans.customer eq customer.user.id }.count()
            }
            assertEquals(1, recordCount)
        }
}
