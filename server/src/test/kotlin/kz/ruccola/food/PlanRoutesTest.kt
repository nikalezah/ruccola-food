package kz.ruccola.food

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
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.Role
import kz.ruccola.food.model.CustomerPlans
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.Plans
import kz.ruccola.food.model.Users
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

    @Test
    fun testCrud() =
        testApplication {
            application { module() }

            // Clean
            suspendTransaction {
                Plans.deleteAll()
            }

            // Create
            val createPayload =
                """
                {"calories":"C1800","periodDays":"D30","pricePerDay":2000,"allowVariantChoice":true}
                """.trimIndent()
            var id = 0
            client.post("/api/plans") {
                contentType(ContentType.Application.Json)
                setBody(createPayload)
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                id = obj["id"]!!.jsonPrimitive.int
                assertEquals("C1800", obj["calories"]!!.jsonPrimitive.content)
                assertTrue(obj["allowVariantChoice"]!!.jsonPrimitive.boolean)
            }

            // List
            client.get("/api/plans").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
            }

            // Update
            val upd = """{"calories":"C2000","allowVariantChoice":false}"""
            client.put("/api/plans/$id") {
                contentType(ContentType.Application.Json)
                setBody(upd)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("C2000", obj["calories"]!!.jsonPrimitive.content)
                assertFalse(obj["allowVariantChoice"]!!.jsonPrimitive.boolean)
            }

            // Delete
            client.delete("/api/plans/$id").apply { assertEquals(HttpStatusCode.OK, status) }
            client.get("/api/plans/$id").apply { assertEquals(HttpStatusCode.NotFound, status) }
        }

    @Test
    fun testCustomerPlanSelection() =
        testApplication {
            application { module() }

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

            val bearerToken = "Bearer dummy-token-$customerId"

            // Test 1: Get a customer plan when none exists
            client.get("/api/customers/plan") {
                header(HttpHeaders.Authorization, bearerToken)
            }.apply {
                assertEquals(HttpStatusCode.NotFound, status)
            }

            // Test 2: Save customer plan
            val today = today().toString()
            val savePlanPayload = """{"planId":$planId1800,"chosenDate":"$today"}"""
            client.post("/api/customers/plan") {
                header(HttpHeaders.Authorization, bearerToken)
                contentType(ContentType.Application.Json)
                setBody(savePlanPayload)
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(customerId, obj["customerId"]!!.jsonPrimitive.int)
                assertEquals(planId1800, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(today, obj["chosenDate"]!!.jsonPrimitive.content)
            }

            // Test 3: Get a customer plan after saving
            client.get("/api/customers/plan") {
                header(HttpHeaders.Authorization, bearerToken)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(customerId, obj["customerId"]!!.jsonPrimitive.int)
                assertEquals(planId1800, obj["plan"]!!.jsonObject["id"]!!.jsonPrimitive.int)
            }

            // Test 4: Update plan with the same date (should replace)
            val updatePlanPayload = """{"planId":$planId2000,"chosenDate":"$today"}"""
            client.post("/api/customers/plan") {
                header(HttpHeaders.Authorization, bearerToken)
                contentType(ContentType.Application.Json)
                setBody(updatePlanPayload)
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
            client.get("/api/plans/calories?allowVariantChoice=true").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
                assertTrue(arr.any { it.jsonPrimitive.int == 1800 })
            }

            // Test 6: Get available calories for without variants
            client.get("/api/plans/calories?allowVariantChoice=false").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
                assertTrue(arr.any { it.jsonPrimitive.int == 2000 })
            }

            // Test 7: Get available days for specific calories with variants
            client.get("/api/plans/days?allowVariantChoice=true&calories=1800").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertTrue(arr.isNotEmpty())
                assertTrue(arr.any { it.jsonPrimitive.int == 30 })
                assertTrue(arr.any { it.jsonPrimitive.int == 14 })
            }

            // Test 8: Get available days without calories parameter (should fail)
            client.get("/api/plans/days?allowVariantChoice=true").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
}
