package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Files
import kz.ruccola.food.now
import kz.ruccola.food.testApp
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
        testApp { client ->
            val token = client.loginAdmin()
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
            client.get("/api/dishes") { authHeader(token) }
                .apply {
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
        testApp { client ->
            val token = client.loginAdmin()
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
            client.get("/api/dishes/$dishId") { authHeader(token) }
                .apply {
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
            client.get("/api/dishes/9999") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.NotFound, status)
                }
        }

    @Test
    fun testCreateDishApi() =
        testApp { client ->
            val token = client.loginAdmin()
            suspendTransaction {
                Dishes.deleteAll()
            }

            val newDish = DishCreateDto(
                name = "Chocolate Cake",
                description = "Rich chocolate cake with ganache frosting",
            )

            client.post("/api/dishes") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(newDish)
            }.apply {
                assertEquals(HttpStatusCode.Created, status)

                val responseText = bodyAsText()
                val jsonObject = Json.parseToJsonElement(responseText).jsonObject

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
        testApp { client ->
            val token = client.loginAdmin()
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
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(updateDish)
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
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(updateDish)
            }.apply {
                assertEquals(HttpStatusCode.NotFound, status)
            }
        }

    @Test
    fun testArchiveDishApi() =
        testApp { client ->
            val token = client.loginAdmin()
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
            client.post("/api/dishes/$dishId/archive") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                }

            // Verify the dish is now archived
            suspendTransaction {
                val dish = Dishes.selectAll().where { Dishes.id eq dishId }.singleOrNull()
                assertNotNull(dish)
                assertTrue(dish[Dishes.archived])
            }

            // Test archive with non-existent ID
            client.post("/api/dishes/9999/archive") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.NotFound, status)
                }
        }

    @Test
    fun testDishImagesCreateAndUpdate() =
        testApp { client ->
            val token = client.loginAdmin()
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

            var dishId = 0
            client.post("/api/dishes") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(DishCreateDto("Image Dish", "Dish with images", listOf(1, 2)))
            }.apply {
                assertEquals(HttpStatusCode.Created, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                dishId = obj["id"]!!.jsonPrimitive.int
                val images = obj["images"]!!.jsonArray
                assertEquals(2, images.size)
                val firstUrl = images[0].jsonObject["url"]!!.jsonPrimitive.content
                assertEquals("/files/1.jpg", firstUrl)
            }

            client.put("/api/dishes/$dishId") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(DishUpdateDto("Image Dish Updated", imageFileIds = listOf(2, 1)))
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
