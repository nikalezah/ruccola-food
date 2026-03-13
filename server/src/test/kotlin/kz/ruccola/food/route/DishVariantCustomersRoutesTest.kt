package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.VariantCustomersPayload
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.DishVariants
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.now
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DishVariantCustomersRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun variantCustomersCrud() =
        testApp { client ->
            val token = client.loginAdmin()
            // Create a dish and variant
            var dishId = 0
            var variantId = 0
            suspendTransaction {
                dishId = Dishes.insertAndGetId {
                    it[name] = "Dish A"
                    it[description] = "Base"
                    it[archived] = false
                }.value
                variantId = DishVariants.insertAndGetId {
                    it[DishVariants.dishId] = dishId
                    it[DishVariants.description] = "No onion"
                    it[DishVariants.createdAt] = now()
                    it[DishVariants.updatedAt] = now()
                }.value
            }

            // Register customers and collect IDs
            val c1 = client.registerCustomer("c1@example.com").user.id
            val c2 = client.registerCustomer("c2@example.com").user.id
            val c3 = client.registerCustomer("c3@example.com").user.id

            // Initially empty
            client.get("/api/dishes/$dishId/variants/$variantId/customers") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    assertEquals(0, arr.size)
                }

            // Set customers [c1, c2]
            val putPayload1 = VariantCustomersPayload(listOf(c1, c2))
            client.put("/api/dishes/$dishId/variants/$variantId/customers") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(putPayload1)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
            }

            // Verify
            client.get("/api/dishes/$dishId/variants/$variantId/customers") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray.map {
                        it.jsonPrimitive.content.toInt()
                    }.toSet()
                    assertEquals(setOf(c1, c2), arr)
                }

            // Replace with [c2, c3]
            val putPayload2 = VariantCustomersPayload(listOf(c2, c3))
            client.put("/api/dishes/$dishId/variants/$variantId/customers") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(putPayload2)
            }.apply { assertEquals(HttpStatusCode.OK, status) }

            client.get("/api/dishes/$dishId/variants/$variantId/customers") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray.map {
                        it.jsonPrimitive.content.toInt()
                    }.toSet()
                    assertEquals(setOf(c2, c3), arr)
                }

            // Negatives: variant not in this dish -> 404 on PUT
            var otherDishId = 0
            suspendTransaction {
                otherDishId = Dishes.insertAndGetId {
                    it[name] = "Dish C"
                    it[description] = "Other"
                    it[archived] = false
                }.value
            }
            val badPut = client.put("/api/dishes/$otherDishId/variants/$variantId/customers") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(putPayload1)
            }
            assertEquals(HttpStatusCode.NotFound, badPut.status)
        }
}
