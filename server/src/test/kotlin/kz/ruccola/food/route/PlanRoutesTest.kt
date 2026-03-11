package kz.ruccola.food.route

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.LoginRequestDto
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.api.Role
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.model.CustomerPlans
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.model.Plans
import kz.ruccola.food.model.Users
import kz.ruccola.food.testApp
import kz.ruccola.food.today
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlanRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    private suspend fun getAdminToken(client: HttpClient): String {
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto("admin@ruccola.food", "admin"))
        }
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return "Bearer ${json["token"]!!.jsonPrimitive.content}"
    }

    private suspend fun getCustomerToken(client: HttpClient): String {
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto("customer@test.com", "password"))
        }
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return "Bearer ${json["token"]!!.jsonPrimitive.content}"
    }

    @Test
    fun testCrud() =
        testApp { client ->

            // Clean
            suspendTransaction {
                Plans.deleteAll()
            }

            // Create
            var id = 0
            val adminToken = getAdminToken(client)
            client.post("/api/plans") {
                header(HttpHeaders.Authorization, adminToken)
                contentType(ContentType.Application.Json)
                setBody(PlanCreateDto(PlanCalories.C1800, PlanDays.D30, 2000, true))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                id = obj["id"]!!.jsonPrimitive.int
                assertEquals("C1800", obj["calories"]!!.jsonPrimitive.content)
                assertTrue(obj["allowVariantChoice"]!!.jsonPrimitive.boolean)
            }

            // List
            client.get("/api/plans") {
                header(HttpHeaders.Authorization, adminToken)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
            }

            // Update
            client.put("/api/plans/$id") {
                header(HttpHeaders.Authorization, adminToken)
                contentType(ContentType.Application.Json)
                setBody(PlanUpdateDto(PlanCalories.C2000, allowVariantChoice = false))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("C2000", obj["calories"]!!.jsonPrimitive.content)
                assertFalse(obj["allowVariantChoice"]!!.jsonPrimitive.boolean)
            }

            // Delete
            client.delete("/api/plans/$id") {
                header(HttpHeaders.Authorization, adminToken)
            }.apply { assertEquals(HttpStatusCode.OK, status) }
            client.get("/api/plans/$id") {
                header(HttpHeaders.Authorization, adminToken)
            }.apply { assertEquals(HttpStatusCode.NotFound, status) }
        }

    @Test
    fun testCustomerPlanSelection() =
        testApp { client ->

            // Setup: Create a customer and plans
            var customerId = 0
            var planId1800 = 0
            var planId2000 = 0

            suspendTransaction {
                Plans.deleteAll()
                CustomerPlans.deleteAll()
                Customers.deleteAll()
                Users.deleteWhere { Users.role eq Role.CUSTOMER }

                val userId = Users.insertAndGetId {
                    it[Users.email] = "customer@test.com"
                    it[Users.password] = "password"
                    it[Users.firstName] = "Test"
                    it[Users.lastName] = "Customer"
                    it[Users.role] = Role.CUSTOMER
                }.value
                customerId = Customers.insertAndGetId {
                    it[Customers.id] = userId
                    it[Customers.address] = "Test Address"
                }.value

                // Create plans with different options
                val planId1 = Plans.insertAndGetId {
                    it[calories] = 1800
                    it[periodDays] = 30
                    it[pricePerDay] = 2000
                    it[allowVariantChoice] = true
                }.value
                planId1800 = planId1

                val planId2 = Plans.insertAndGetId {
                    it[calories] = 2000
                    it[periodDays] = 14
                    it[pricePerDay] = 2500
                    it[allowVariantChoice] = false
                }.value
                planId2000 = planId2

                // Additional plans for options testing
                Plans.insert {
                    it[calories] = 1800
                    it[periodDays] = 14
                    it[pricePerDay] = 1800
                    it[allowVariantChoice] = true
                }
                Plans.insert {
                    it[calories] = 2200
                    it[periodDays] = 30
                    it[pricePerDay] = 2200
                    it[allowVariantChoice] = false
                }
            }

            // Test 1: Get a customer plan when none exists
            client.get("/api/customers/plan") {
                header(HttpHeaders.Authorization, getCustomerToken(client))
            }.apply {
                assertEquals(HttpStatusCode.NotFound, status)
            }

            // Test 2: Save customer plan
            val today = today()
            val customerToken = getCustomerToken(client)
            client.post("/api/customers/plan") {
                header(HttpHeaders.Authorization, customerToken)
                contentType(ContentType.Application.Json)
                setBody(CustomerPlanCreateDto(planId1800, today))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(customerId, obj["customerId"]!!.jsonPrimitive.int)
                assertEquals(planId1800, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(today.toString(), obj["chosenDate"]!!.jsonPrimitive.content)
            }

            // Test 3: Get a customer plan after saving
            client.get("/api/customers/plan") {
                header(HttpHeaders.Authorization, customerToken)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(customerId, obj["customerId"]!!.jsonPrimitive.int)
                assertEquals(planId1800, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
            }

            // Test 4: Update plan with the same date (should replace)
            client.post("/api/customers/plan") {
                header(HttpHeaders.Authorization, customerToken)
                contentType(ContentType.Application.Json)
                setBody(CustomerPlanCreateDto(planId2000, today))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(planId2000, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
            }

            // Verify only one record exists for today
            val recordCount = suspendTransaction {
                CustomerPlans.selectAll().where { CustomerPlans.customer eq customerId }.count()
            }
            assertEquals(1, recordCount)

            // Test 5: Get available calories for with variants
            client.get("/api/plans/calories?allowVariantChoice=true") {
                header(HttpHeaders.Authorization, customerToken)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
                assertTrue(arr.any { it.jsonPrimitive.int == 1800 })
            }

            // Test 6: Get available calories for without variants
            client.get("/api/plans/calories?allowVariantChoice=false") {
                header(HttpHeaders.Authorization, customerToken)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
                assertTrue(arr.any { it.jsonPrimitive.int == 2000 })
            }

            // Test 7: Get available days for specific calories with variants
            client.get("/api/plans/days?allowVariantChoice=true&calories=1800") {
                header(HttpHeaders.Authorization, customerToken)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
                assertTrue(arr.any { it.jsonPrimitive.int == 30 })
                assertTrue(arr.any { it.jsonPrimitive.int == 14 })
            }

            // Test 8: Get available days without calories parameter (should fail)
            client.get("/api/plans/days?allowVariantChoice=true") {
                header(HttpHeaders.Authorization, customerToken)
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
}
