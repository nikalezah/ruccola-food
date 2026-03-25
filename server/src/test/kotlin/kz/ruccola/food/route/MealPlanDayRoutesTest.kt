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
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.api.MealPlanDaysReorderDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.Meal
import kz.ruccola.food.testApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MealPlanDayRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testListSaveDelete() =
        testApp { client ->
            val token = client.loginAdmin()

            client.get("/api/meal-plan-days") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    assertEquals(0, arr.size)
                }

            val dishId1 = Json.parseToJsonElement(
                client.post("/api/dishes") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(DishCreateDto(name = "Test Dish", description = "Desc"))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            val mealPlanDayId = client.put("/api/meal-plan-days") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaySaveDto(null, mapOf(dishId1 to Meal.BREAKFAST)))
            }.let {
                assertEquals(HttpStatusCode.OK, it.status)
                val obj = Json.parseToJsonElement(it.bodyAsText()).jsonObject
                assertEquals(1, obj["dishes"]!!.jsonArray.size)

                val dish1 = obj["dishes"]!!.jsonArray[0].jsonObject
                assertEquals(dishId1, dish1.jsonObject["dish"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(Meal.BREAKFAST.name, dish1.jsonObject["meal"]!!.jsonPrimitive.content)

                obj["id"]!!.jsonPrimitive.int
            }

            client.get("/api/meal-plan-days") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    assertEquals(1, arr.size)

                    val mdp = arr[0].jsonObject
                    val dishes = mdp["dishes"]!!.jsonArray
                    assertEquals(1, dishes.size)
                    assertEquals(dishId1, dishes[0].jsonObject["dish"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                    assertEquals(Meal.BREAKFAST.name, dishes[0].jsonObject["meal"]!!.jsonPrimitive.content)
                }

            val dishId2 = Json.parseToJsonElement(
                client.post("/api/dishes") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(DishCreateDto(name = "Test Dish Two", description = "Desc 2"))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            client.put("/api/meal-plan-days") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaySaveDto(mealPlanDayId, mapOf(dishId1 to Meal.BREAKFAST, dishId2 to Meal.BRUNCH)))
            }.apply {
                assertEquals(HttpStatusCode.OK, status)
                val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
                assertEquals(2, obj["dishes"]!!.jsonArray.size)

                val dish1 = obj["dishes"]!!.jsonArray[0].jsonObject
                assertEquals(dishId1, dish1.jsonObject["dish"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(Meal.BREAKFAST.name, dish1.jsonObject["meal"]!!.jsonPrimitive.content)

                val dish2 = obj["dishes"]!!.jsonArray[1].jsonObject
                assertEquals(dishId2, dish2.jsonObject["dish"]!!.jsonObject["id"]!!.jsonPrimitive.int)
                assertEquals(Meal.BRUNCH.name, dish2.jsonObject["meal"]!!.jsonPrimitive.content)
            }

            client.delete("/api/meal-plan-days/$mealPlanDayId") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }
            client.delete("/api/meal-plan-days/$mealPlanDayId") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }
            client.delete("/api/meal-plan-days/abc") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.BadRequest, status) }
        }

    @Test
    fun testSetCurrentSwitching() =
        testApp { client ->
            val token = client.loginAdmin()

            val id1 = Json.parseToJsonElement(
                client.put("/api/meal-plan-days") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(MealPlanDaySaveDto(null, mapOf()))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            val id2 = Json.parseToJsonElement(
                client.put("/api/meal-plan-days") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(MealPlanDaySaveDto(null, mapOf()))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            // set first current
            client.post("/api/meal-plan-days/$id1/current") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }
            // verify only one current
            client.get("/api/meal-plan-days") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    val currentCount = arr.count { it.jsonObject["current"]?.jsonPrimitive?.boolean == true }
                    assertEquals(1, currentCount)
                    val currentId =
                        arr.first {
                            it.jsonObject["current"]?.jsonPrimitive?.boolean == true
                        }.jsonObject["id"]!!.jsonPrimitive.int
                    assertEquals(id1, currentId)
                }

            // set the second current; the first should be unset
            client.post("/api/meal-plan-days/$id2/current") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }
            client.get("/api/meal-plan-days") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    val currentCount = arr.count { it.jsonObject["current"]?.jsonPrimitive?.boolean == true }
                    assertEquals(1, currentCount)
                    val currentId =
                        arr.first {
                            it.jsonObject["current"]?.jsonPrimitive?.boolean == true
                        }.jsonObject["id"]!!.jsonPrimitive.int
                    assertEquals(id2, currentId)
                }
        }

    @Test
    fun testBulkReorderSuccess() =
        testApp { client ->
            val token = client.loginAdmin()

            val id1 = Json.parseToJsonElement(
                client.put("/api/meal-plan-days") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(MealPlanDaySaveDto(null, mapOf()))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            val id2 = Json.parseToJsonElement(
                client.put("/api/meal-plan-days") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(MealPlanDaySaveDto(null, mapOf()))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            val id3 = Json.parseToJsonElement(
                client.put("/api/meal-plan-days") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(MealPlanDaySaveDto(null, mapOf()))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            // reorder: [id3, id1, id2]
            client.post("/api/meal-plan-days/reorder") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(listOf(id3, id1, id2)))
            }.apply { assertEquals(HttpStatusCode.OK, status) }

            // verify order is id3(1), id1(2), id2(3)
            client.get("/api/meal-plan-days") { authHeader(token) }
                .apply {
                    assertEquals(HttpStatusCode.OK, status)
                    val arr = Json.parseToJsonElement(bodyAsText()).jsonArray
                    assertEquals(3, arr.size)
                    val first = arr[0].jsonObject
                    val second = arr[1].jsonObject
                    val third = arr[2].jsonObject
                    assertEquals(id3, first["id"]!!.jsonPrimitive.int)
                    assertEquals(1, first["serial"]!!.jsonPrimitive.int)
                    assertEquals(id1, second["id"]!!.jsonPrimitive.int)
                    assertEquals(2, second["serial"]!!.jsonPrimitive.int)
                    assertEquals(id2, third["id"]!!.jsonPrimitive.int)
                    assertEquals(3, third["serial"]!!.jsonPrimitive.int)
                }
        }

    @Test
    fun testBulkReorderValidation() =
        testApp { client ->
            val token = client.loginAdmin()

            val id1 = Json.parseToJsonElement(
                client.put("/api/meal-plan-days") {
                    authHeader(token)
                    contentType(ContentType.Application.Json)
                    setBody(MealPlanDaySaveDto(null, mapOf()))
                }.bodyAsText(),
            ).jsonObject["id"]!!.jsonPrimitive.int

            // duplicate id -> not a permutation => 400
            client.post("/api/meal-plan-days/reorder") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(listOf(id1, id1)))
            }.apply { assertEquals(HttpStatusCode.BadRequest, status) }

            // missing one id -> not a permutation => 400
            client.post("/api/meal-plan-days/reorder") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(listOf(id1)))
            }.apply { assertEquals(HttpStatusCode.BadRequest, status) }

            // includes non-existent id -> 404
            client.post("/api/meal-plan-days/reorder") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(listOf(id1, 999999)))
            }.apply { assertEquals(HttpStatusCode.NotFound, status) }
        }
}
