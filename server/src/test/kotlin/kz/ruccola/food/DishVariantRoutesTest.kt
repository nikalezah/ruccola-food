package kz.ruccola.food

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kz.ruccola.food.model.Dishes
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DishVariantRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testDishVariantCrud() =
        testApplication {
            application { module() }

            var dishId = 0
            suspendTransaction {
                Dishes.deleteAll()
                dishId = Dishes.insertAndGetId {
                    it[name] = "Variant Dish"
                    it[description] = "Base description"
                    it[archived] = false
                }.value
            }

            // Initially, variants are empty
            client.get("/api/dishes/$dishId/variants").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(0, arr.size)
            }

            // Create
            val newPayload = buildJsonObject { put("description", "No onion") }
            var variantId = 0
            client.post("/api/dishes/$dishId/variants") {
                contentType(ContentType.Application.Json)
                setBody(newPayload.toString())
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("No onion", obj["description"]!!.jsonPrimitive.content)
                variantId = obj["id"]!!.jsonPrimitive.int
            }

            // List should have one
            client.get("/api/dishes/$dishId/variants").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(1, arr.size)
            }

            // Update
            val updatePayload = buildJsonObject { put("description", "Extra spicy") }
            client.put("/api/dishes/$dishId/variants/$variantId") {
                contentType(ContentType.Application.Json)
                setBody(updatePayload.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("Extra spicy", obj["description"]!!.jsonPrimitive.content)
            }

            // Delete
            client.delete("/api/dishes/$dishId/variants/$variantId").apply {
                assertEquals(HttpStatusCode.OK, status)
            }

            // List back to empty
            client.get("/api/dishes/$dishId/variants").apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(0, arr.size)
            }
        }
}
