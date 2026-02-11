package kz.ruccola.food

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Files
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DishRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testGetDishesApi() =
        testApplication {
            // Set up the test environment
            application { module() }

            // Create test data
            suspendTransaction {
                Dishes.deleteAll()
                Dishes.insert {
                    it[name] = "Pasta Carbonara"
                    it[description] = "Classic Italian pasta dish with eggs, cheese, pancetta, and pepper"
                    it[archived] = false
                }
                Dishes.insert {
                    it[name] = "Caesar Salad"
                    it[description] = "Fresh romaine lettuce with Caesar dressing, croutons, and parmesan"
                    it[archived] = false
                }
            }

            // Test GET /api/dishes
            client.get("/api/dishes").apply {
                assertEquals(HttpStatusCode.OK, status)

                val responseText = bodyAsText()
                val jsonArray = Json.parseToJsonElement(responseText).jsonArray

                // Verify we have 2 dishes
                assertEquals(2, jsonArray.size)

                // Verify the first dish has the expected properties
                val firstDish = jsonArray[0].jsonObject
                assertTrue(firstDish.containsKey("id"))
                assertTrue(firstDish.containsKey("name"))
                assertTrue(firstDish.containsKey("description"))
                assertTrue(firstDish.containsKey("archived"))
                assertTrue(firstDish.containsKey("createdAt"))
                assertTrue(firstDish.containsKey("updatedAt"))
            }
        }

    @Test
    fun testGetDishByIdApi() =
        testApplication {
            // Set up the test environment
            application { module() }

            var dishId = 0

            // Create test data
            suspendTransaction {
                Dishes.deleteAll()
                dishId = Dishes.insertAndGetId {
                    it[name] = "Sushi Roll"
                    it[description] = "Fresh fish and vegetables wrapped in rice and seaweed"
                    it[archived] = false
                }.value
            }

            // Test GET /api/dishes/{id}
            client.get("/api/dishes/$dishId").apply {
                assertEquals(HttpStatusCode.OK, status)

                val responseText = bodyAsText()
                val jsonObject = Json.parseToJsonElement(responseText).jsonObject

                // Verify the dish has the expected properties
                assertEquals(dishId, jsonObject["id"]?.jsonPrimitive?.int)
                assertEquals("Sushi Roll", jsonObject["name"]?.jsonPrimitive?.content)
                assertEquals(
                    "Fresh fish and vegetables wrapped in rice and seaweed",
                    jsonObject["description"]?.jsonPrimitive?.content,
                )
                assertEquals(false, jsonObject["archived"]?.jsonPrimitive?.boolean)
            }

            // Test GET with non-existent ID
            client.get("/api/dishes/9999").apply {
                assertEquals(HttpStatusCode.NotFound, status)
            }
        }

    @Test
    fun testCreateDishApi() =
        testApplication {
            // Set up the test environment
            application { module() }

            // Clear any existing dishes
            suspendTransaction {
                Dishes.deleteAll()
            }

            // Test POST /api/dishes
            val newDish = DishCreateDto(
                name = "Chocolate Cake",
                description = "Rich chocolate cake with ganache frosting",
            )

            client.post("/api/dishes") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(DishCreateDto.serializer(), newDish))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)

                val responseText = bodyAsText()
                val jsonObject = Json.parseToJsonElement(responseText).jsonObject

                // Verify the created dish has the expected properties
                assertEquals("Chocolate Cake", jsonObject["name"]?.jsonPrimitive?.content)
                assertEquals(
                    "Rich chocolate cake with ganache frosting",
                    jsonObject["description"]?.jsonPrimitive?.content,
                )
                assertEquals(false, jsonObject["archived"]?.jsonPrimitive?.boolean)
            }
        }

    @Test
    fun testUpdateDishApi() =
        testApplication {
            // Set up the test environment
            application { module() }

            var dishId = 0

            // Create test data
            suspendTransaction {
                Dishes.deleteAll()
                dishId = Dishes.insertAndGetId {
                    it[name] = "Pizza Margherita"
                    it[description] = "Classic pizza with tomato sauce, mozzarella, and basil"
                    it[archived] = false
                }.value
            }

            // Test PUT /api/dishes/{id}
            val updateDish = DishUpdateDto(
                name = "Pizza Margherita Deluxe",
                description = "Classic pizza with premium tomato sauce, buffalo mozzarella, and fresh basil",
            )

            client.put("/api/dishes/$dishId") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(DishUpdateDto.serializer(), updateDish))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)

                val responseText = bodyAsText()
                val jsonObject = Json.parseToJsonElement(responseText).jsonObject

                // Verify the updated dish has the expected properties
                assertEquals(dishId, jsonObject["id"]?.jsonPrimitive?.int)
                assertEquals("Pizza Margherita Deluxe", jsonObject["name"]?.jsonPrimitive?.content)
                assertEquals(
                    "Classic pizza with premium tomato sauce, buffalo mozzarella, and fresh basil",
                    jsonObject["description"]?.jsonPrimitive?.content,
                )
            }

            // Test PUT with non-existent ID
            client.put("/api/dishes/9999") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(DishUpdateDto.serializer(), updateDish))
            }.apply {
                assertEquals(HttpStatusCode.NotFound, status)
            }
        }

    @Test
    fun testArchiveDishApi() =
        testApplication {
            // Set up the test environment
            application { module() }

            var dishId = 0

            // Create test data
            suspendTransaction {
                Dishes.deleteAll()
                dishId = Dishes.insertAndGetId {
                    it[name] = "Beef Burger"
                    it[description] = "Juicy beef patty with lettuce, tomato, and special sauce"
                    it[archived] = false
                }.value
            }

            // Test POST /api/dishes/{id}/archive
            client.post("/api/dishes/$dishId/archive").apply {
                assertEquals(HttpStatusCode.OK, status)
            }

            // Verify the dish is now archived
            suspendTransaction {
                val dish = Dishes.selectAll().where { Dishes.id eq dishId }.singleOrNull()
                assertNotNull(dish)
                assertTrue(dish[Dishes.archived])
            }

            // Test archive with non-existent ID
            client.post("/api/dishes/9999/archive").apply {
                assertEquals(HttpStatusCode.NotFound, status)
            }
        }

    @Test
    fun testDishImagesCreateAndUpdate() =
        testApplication {
            application { module() }

            // Ensure a clean state
            suspendTransaction {
                Dishes.deleteAll()
            }

            suspendTransaction {
                listOf(1L, 2L).forEach { i ->
                    Files.insert {
                        it[filename] = "$i.jpg"
                        it[path] = "/$i.jpg"
                        it[mimeType] = "image/jpeg"
                        it[size] = i
                        it[createdAt] = now()
                    }
                }
            }

            // Create with images (order defines primary: first is primary)
            val payload = buildJsonObject {
                put("name", "Image Dish")
                put("description", "Dish with images")
                putJsonArray("imageFileIds") {
                    add(1)
                    add(2)
                }
            }

            var dishId = 0
            client.post("/api/dishes") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                dishId = obj["id"]!!.jsonPrimitive.int
                val images = obj["images"]!!.jsonArray
                assertEquals(2, images.size)
                val firstUrl = images[0].jsonObject["url"]!!.jsonPrimitive.content
                assertEquals("/files/1.jpg", firstUrl)
            }

            // Update: replace images set, ensure only one primary
            val updatePayload = buildJsonObject {
                put("name", "Image Dish Updated")
                putJsonArray("imageFileIds") {
                    // Reorder to make file 2 primary (first in the list)
                    add(2)
                    add(1)
                }
            }

            client.put("/api/dishes/$dishId") {
                contentType(ContentType.Application.Json)
                setBody(updatePayload.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals("Image Dish Updated", obj["name"]!!.jsonPrimitive.content)
                val images = obj["images"]!!.jsonArray
                assertEquals(2, images.size)
                val firstUrl = images[0].jsonObject["url"]!!.jsonPrimitive.content
                assertEquals("/files/2.jpg", firstUrl)
            }
        }
}
