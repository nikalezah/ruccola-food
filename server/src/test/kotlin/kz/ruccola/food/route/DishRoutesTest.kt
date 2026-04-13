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
import kz.ruccola.food.api.DishTranslation
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.localization.Language
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.DishTranslations
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
            suspendTransaction {
                Dishes.deleteAll()
                DishTranslations.deleteAll()
                val dishId = Dishes.insertAndGetId {
                    it[archived] = false
                }.value
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = when (lang) {
                            Language.EN -> "Pasta Carbonara"
                            Language.RU -> "Паста Карбонара"
                            Language.KK -> "Паста Карбонара"
                        }
                        it[DishTranslations.description] = when (lang) {
                            Language.EN -> "Classic Italian pasta"
                            Language.RU -> "Классическая итальянская паста"
                            Language.KK -> "Классикалық итальяндық паста"
                        }
                    }
                }
                val dishId2 = Dishes.insertAndGetId {
                    it[archived] = false
                }.value
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId2
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = when (lang) {
                            Language.EN -> "Caesar Salad"
                            Language.RU -> "Цезарь"
                            Language.KK -> "Цезарь"
                        }
                        it[DishTranslations.description] = when (lang) {
                            Language.EN -> "Fresh romaine lettuce"
                            Language.RU -> "Свежий салат ромен"
                            Language.KK -> "Жаңа ромен салаты"
                        }
                    }
                }
            }

            client.get("/api/dishes") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)

                    val responseText = bodyAsText()
                    val jsonObject = Json.parseToJsonElement(responseText).jsonObject
                    val jsonArray = jsonObject["items"]!!.jsonArray

                    assertEquals(2, jsonArray.size)
                    assertEquals(2, jsonObject["totalCount"]!!.jsonPrimitive.int)

                    val firstDish = jsonArray[0].jsonObject
                    assertTrue(firstDish.containsKey("id"))
                    assertTrue(firstDish.containsKey("name"))
                    assertTrue(firstDish.containsKey("description"))
                    assertTrue(firstDish.containsKey("archived"))
                    assertTrue(firstDish.containsKey("createdAt"))
                    assertTrue(firstDish.containsKey("updatedAt"))
                    assertTrue(firstDish.containsKey("images"))

                    val name = firstDish["name"]!!.jsonPrimitive.content
                    assertTrue(name.isNotEmpty())
                }
        }

    @Test
    fun testCreateDishApi() =
        testApp { client ->
            val token = client.loginAdmin()
            suspendTransaction {
                Dishes.deleteAll()
                DishTranslations.deleteAll()
            }

            val newDish = DishCreateDto(
                translations = mapOf(
                    Language.EN to DishTranslation("Chocolate Cake", "Rich chocolate cake"),
                    Language.RU to DishTranslation("Шоколадный торт", "Шоколадный торт с ганашем"),
                    Language.KK to DishTranslation("Шоколадты торт", "Ганашты шоколадты торт"),
                ),
            )

            client.post("/api/dishes") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(newDish)
            }.apply {
                assertEquals(HttpStatusCode.Created, status)

                val responseText = bodyAsText()
                val jsonObject = Json.parseToJsonElement(responseText).jsonObject

                val translations = jsonObject["translations"]!!.jsonObject
                assertEquals("Chocolate Cake", translations["EN"]!!.jsonObject["name"]!!.jsonPrimitive.content)
                assertEquals(
                    "Rich chocolate cake",
                    translations["EN"]!!.jsonObject["description"]!!.jsonPrimitive.content,
                )
                assertEquals(false, jsonObject["archived"]?.jsonPrimitive?.boolean)
            }
        }

    @Test
    fun testCreateDishMissingTranslations() =
        testApp { client ->
            val token = client.loginAdmin()
            suspendTransaction {
                Dishes.deleteAll()
                DishTranslations.deleteAll()
            }

            val newDish = DishCreateDto(
                translations = mapOf(
                    Language.EN to DishTranslation("Chocolate Cake", "Rich chocolate cake"),
                ),
            )

            client.post("/api/dishes") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(newDish)
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                val responseText = bodyAsText()
                assertTrue(responseText.contains("Missing translations"))
            }
        }

    @Test
    fun testUpdateDishApi() =
        testApp { client ->
            val token = client.loginAdmin()
            var dishId = 0

            suspendTransaction {
                Dishes.deleteAll()
                DishTranslations.deleteAll()
                dishId = Dishes.insertAndGetId {
                    it[archived] = false
                }.value
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = when (lang) {
                            Language.EN -> "Pizza Margherita"
                            Language.RU -> "Пицца Маргарита"
                            Language.KK -> "Пицца Маргарита"
                        }
                        it[DishTranslations.description] = when (lang) {
                            Language.EN -> "Classic pizza"
                            Language.RU -> "Классическая пицца"
                            Language.KK -> "Классикалық пицца"
                        }
                    }
                }
            }

            val updateDish = DishUpdateDto(
                translations = mapOf(
                    Language.EN to DishTranslation("Pizza Margherita Deluxe", "Premium pizza"),
                    Language.RU to DishTranslation("Пицца Маргарита Делюкс", "Премиум пицца"),
                    Language.KK to DishTranslation("Пицца Маргарита Делюкс", "Премиум пицца"),
                ),
            )

            client.put("/api/dishes/$dishId") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(updateDish)
            }.apply {
                assertEquals(HttpStatusCode.OK, status)

                val responseText = bodyAsText()
                val jsonObject = Json.parseToJsonElement(responseText).jsonObject

                assertEquals(dishId, jsonObject["id"]?.jsonPrimitive?.int)
                val translations = jsonObject["translations"]!!.jsonObject
                assertEquals("Pizza Margherita Deluxe", translations["EN"]!!.jsonObject["name"]!!.jsonPrimitive.content)
            }

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

            suspendTransaction {
                Dishes.deleteAll()
                DishTranslations.deleteAll()
                dishId = Dishes.insertAndGetId {
                    it[archived] = false
                }.value
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = when (lang) {
                            Language.EN -> "Beef Burger"
                            Language.RU -> "Бургер"
                            Language.KK -> "Бургер"
                        }
                        it[DishTranslations.description] = when (lang) {
                            Language.EN -> "Juicy beef patty"
                            Language.RU -> "Сочная говядина"
                            Language.KK -> "Нандық ет"
                        }
                    }
                }
            }

            client.post("/api/dishes/$dishId/archive") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                }

            suspendTransaction {
                val dish = Dishes.selectAll().where { Dishes.id eq dishId }.singleOrNull()
                assertNotNull(dish)
                assertTrue(dish[Dishes.archived])
            }

            client.post("/api/dishes/9999/archive") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.NotFound, status)
                }
        }

    @Test
    fun testDishImagesCreateAndUpdate() =
        testApp { client ->
            val token = client.loginAdmin()
            suspendTransaction {
                Dishes.deleteAll()
                DishTranslations.deleteAll()
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
                setBody(
                    DishCreateDto(
                        translations = mapOf(
                            Language.EN to DishTranslation("Image Dish", "Dish with images"),
                            Language.RU to DishTranslation("Блюдо с изображением", "Блюдо с изображениями"),
                            Language.KK to DishTranslation("Суретті тағам", "Суреттермен тағам"),
                        ),
                        imageFileIds = listOf(1, 2),
                    ),
                )
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
                setBody(
                    DishUpdateDto(
                        translations = mapOf(
                            Language.EN to DishTranslation("Image Dish Updated", "Updated dish with images"),
                            Language.RU to DishTranslation("Обновленное блюдо", "Обновленное блюдо с изображениями"),
                            Language.KK to DishTranslation("Жаңартылған тағам", "Жаңартылған суреттермен тағам"),
                        ),
                        imageFileIds = listOf(2, 1),
                    ),
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                val translations = obj["translations"]!!.jsonObject
                assertEquals("Image Dish Updated", translations["EN"]!!.jsonObject["name"]!!.jsonPrimitive.content)
                val images = obj["images"]!!.jsonArray
                assertEquals(2, images.size)
                val firstUrl = images[0].jsonObject["url"]!!.jsonPrimitive.content
                assertEquals("/files/2.jpg", firstUrl)
            }
        }
}
