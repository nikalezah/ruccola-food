package kz.ruccola.food

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.model.DishVariants
import kz.ruccola.food.model.Dishes
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DishVariantCustomersRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    private suspend fun registerCustomer(
        client: io.ktor.client.HttpClient,
        email: String,
    ): Int {
        val registerResp = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "email": "$email",
                  "password": "secret",
                  "confirmPassword": "secret",
                  "firstName": "John",
                  "lastName": "Doe",
                  "address": "123 Main St"
                }
                """.trimIndent(),
            )
        }
        assertTrue(registerResp.status.isSuccess())
        val json = Json.parseToJsonElement(registerResp.bodyAsText()).jsonObject
        // Response is AuthResponseDto { token, user }
        val user = json["user"]!!.jsonObject
        return user["id"]!!.jsonPrimitive.content.toInt()
    }

    @Test
    fun variantCustomersCrud() =
        testApplication {
            application { module() }

            // Clean slate and create a dish and variant
            var dishId = 0
            var variantId = 0
            suspendTransaction {
                DishVariants.deleteAll()
                Dishes.deleteAll()
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
            val c1 = registerCustomer(client, "c1@example.com")
            val c2 = registerCustomer(client, "c2@example.com")
            val c3 = registerCustomer(client, "c3@example.com")

            // Initially empty
            client.get("/api/dishes/$dishId/variants/$variantId/customers").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(0, arr.size)
            }

            // Set customers [c1, c2]
            val putPayload1 = "{\"customerIds\":[$c1,$c2]}"
            client.put("/api/dishes/$dishId/variants/$variantId/customers") {
                contentType(ContentType.Application.Json)
                setBody(putPayload1)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
            }

            // Verify
            client.get("/api/dishes/$dishId/variants/$variantId/customers").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray.map {
                    it.jsonPrimitive.content.toInt()
                }.toSet()
                assertEquals(setOf(c1, c2), arr)
            }

            // Replace with [c2, c3]
            val putPayload2 = "{\"customerIds\":[$c2,$c3]}"
            client.put("/api/dishes/$dishId/variants/$variantId/customers") {
                contentType(ContentType.Application.Json)
                setBody(putPayload2)
            }.apply { assertEquals(HttpStatusCode.OK, status) }

            client.get("/api/dishes/$dishId/variants/$variantId/customers").apply {
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
                contentType(ContentType.Application.Json)
                setBody(putPayload1)
            }
            assertEquals(HttpStatusCode.NotFound, badPut.status)
        }
}
