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
                setBody(PlanUpdateDto(5000))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(5000, obj["pricePerDay"]!!.jsonPrimitive.int)
            }

            // Delete
            client.delete("/api/plans/$id") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }
            client.get("/api/plans/$id") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.NotFound, status) }
        }

    @Test
    fun testDuplicatePlan() =
        testApp { client ->
            val token = client.loginAdmin()
            client.post("/api/plans") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(PlanCreateDto(PlanCalories.C1800, PlanDays.D30, 2000))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
            }

            client.post("/api/plans") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(PlanCreateDto(PlanCalories.C1800, PlanDays.D30, 2500))
            }.apply {
                assertEquals(HttpStatusCode.Conflict, status)
                assertEquals("A plan with these calories and days already exists", bodyAsText())
            }
        }

    @Test
    fun testCustomerPlanSelection() =
        testApp { client ->
            // Setup: Create a customer and plans
            val customer = client.registerCustomer()
            var planId1 = 0
            var planId2 = 0

            suspendTransaction {
                // Create plans with different options
                planId1 = Plans.insertAndGetId {
                    it[calories] = 1200
                    it[periodDays] = 30
                    it[pricePerDay] = 2000
                }.value

                planId2 = Plans.insertAndGetId {
                    it[calories] = 1500
                    it[periodDays] = 14
                    it[pricePerDay] = 2500
                }.value

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
                setBody(CustomerPlanCreateDto(planId1, today))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(customer.user.id, obj["customerId"]!!.jsonPrimitive.int)
                assertEquals(planId1, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(today.toString(), obj["chosenDate"]!!.jsonPrimitive.content)
            }

            // Test 3: Get a customer plan after saving
            client.get("/api/customers/plan") { authHeader(customer.token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                    assertEquals(customer.user.id, obj["customerId"]!!.jsonPrimitive.int)
                    assertEquals(planId1, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                }

            // Test 4: Update plan with the same date (should replace)
            client.post("/api/customers/plan") {
                authHeader(customer.token)
                contentType(ContentType.Application.Json)
                setBody(CustomerPlanCreateDto(planId2, today))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(planId2, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
            }

            // Verify only one record exists for today
            val recordCount = suspendTransaction {
                CustomerPlans.selectAll().where { CustomerPlans.customer eq customer.user.id }.count()
            }
            assertEquals(1, recordCount)
        }
}
