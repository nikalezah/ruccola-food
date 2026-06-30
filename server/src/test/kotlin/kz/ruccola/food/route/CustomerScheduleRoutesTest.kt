package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.authHeader
import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.DishTranslations
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Meal
import kz.ruccola.food.model.MealPlanDayDishes
import kz.ruccola.food.model.MealPlanDays
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomerScheduleRoutesTest : RouteIntegrationTest() {
    @Test
    fun testCustomerWeekPlanWrapsAndHasSevenItems() =
        testApp { client ->
            val token = client.registerCustomer().token

            suspendTransaction {
                val mpId1 = MealPlanDays.insertAndGetId {
                    it[serial] = 1
                    it[current] = false
                }
                val mpId2 = MealPlanDays.insertAndGetId {
                    it[serial] = 2
                    it[current] = true
                }
                val mpId3 = MealPlanDays.insertAndGetId {
                    it[serial] = 3
                    it[current] = false
                }

                val dishId1 = Dishes.insertAndGetId {
                    it[archived] = false
                }
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId1
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = if (lang == Language.EN) "A" else "А"
                        it[DishTranslations.description] = "d"
                    }
                }

                val dishId2 = Dishes.insertAndGetId {
                    it[archived] = false
                }
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId2
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = if (lang == Language.EN) "B" else "В"
                        it[DishTranslations.description] = "d"
                    }
                }

                val dishId3 = Dishes.insertAndGetId {
                    it[archived] = false
                }
                Language.entries.forEach { lang ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId3
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = if (lang == Language.EN) "C" else "С"
                        it[DishTranslations.description] = "d"
                    }
                }

                MealPlanDayDishes.insert {
                    it[MealPlanDayDishes.mealPlanDayId] = mpId2
                    it[MealPlanDayDishes.dishId] = dishId2
                    it[MealPlanDayDishes.meal] = Meal.BREAKFAST.name
                }
                MealPlanDayDishes.insert {
                    it[MealPlanDayDishes.mealPlanDayId] = mpId3
                    it[MealPlanDayDishes.dishId] = dishId3
                    it[MealPlanDayDishes.meal] = Meal.BREAKFAST.name
                }
                MealPlanDayDishes.insert {
                    it[MealPlanDayDishes.mealPlanDayId] = mpId1
                    it[MealPlanDayDishes.dishId] = dishId1
                    it[MealPlanDayDishes.meal] = Meal.BREAKFAST.name
                }
            }

            val pageSize = 7
            val resp = client.get("/api/customers/schedule") {
                authHeader(token)
                parameter("page", 0)
                parameter("size", pageSize)
            }
            assertEquals(HttpStatusCode.OK, resp.status)
            val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
            val arr = json["items"]!!.jsonArray
            assertEquals(pageSize, arr.size)
            assertEquals(pageSize, json["totalCount"]!!.jsonPrimitive.content.toInt())
            assertEquals(0, json["page"]!!.jsonPrimitive.content.toInt())
            assertEquals(pageSize, json["size"]!!.jsonPrimitive.content.toInt())
            val day0Dishes = arr[0].jsonObject["dishes"]!!.jsonArray
            val names0 = day0Dishes.map { it.jsonObject["dish"]!!.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names0.contains("В"))
            val day1Dishes = arr[1].jsonObject["dishes"]!!.jsonArray
            val names1 = day1Dishes.map { it.jsonObject["dish"]!!.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names1.contains("С"))
            val day2Dishes = arr[2].jsonObject["dishes"]!!.jsonArray
            val names2 = day2Dishes.map { it.jsonObject["dish"]!!.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names2.contains("А"))
        }

    @Test
    fun testCustomerWeekPlanWithNoMealPlanDays() =
        testApp { client ->
            val token = client.registerCustomer().token
            val pageSize = 7
            val resp = client.get("/api/customers/schedule") {
                authHeader(token)
                parameter("page", 0)
                parameter("size", pageSize)
            }
            assertEquals(HttpStatusCode.OK, resp.status)
            val json = Json.parseToJsonElement(resp.bodyAsText()).jsonObject
            val arr = json["items"]!!.jsonArray
            assertEquals(pageSize, arr.size)
            arr.forEach { day ->
                assertEquals(0, day.jsonObject["dishes"]!!.jsonArray.size)
            }
        }
}
