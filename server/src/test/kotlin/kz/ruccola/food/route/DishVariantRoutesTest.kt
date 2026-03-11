package kz.ruccola.food.route

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
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.DishVariantSaveDto
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.testApp
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
        testApp { client ->
            val token = client.loginAdmin()
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
            client.get("/api/dishes/$dishId/variants") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(0, arr.size)
            }

            // Create
            var variantId = 0
            client.post("/api/dishes/$dishId/variants") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(DishVariantSaveDto("No onion"))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("No onion", obj["description"]!!.jsonPrimitive.content)
                variantId = obj["id"]!!.jsonPrimitive.int
            }

            // List should have one
            client.get("/api/dishes/$dishId/variants") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(1, arr.size)
            }

            // Update
            client.put("/api/dishes/$dishId/variants/$variantId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(DishVariantSaveDto("Extra spicy"))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("Extra spicy", obj["description"]!!.jsonPrimitive.content)
            }

            // Delete
            client.delete("/api/dishes/$dishId/variants/$variantId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
            }

            // List back to empty
            client.get("/api/dishes/$dishId/variants") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                assertEquals(0, arr.size)
            }
        }
}
