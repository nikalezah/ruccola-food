package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Meal
import kz.ruccola.food.model.MealPlanDayDishes
import kz.ruccola.food.model.MealPlanDays
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomerWeekRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testCustomerWeekPlanWrapsAndHasSevenItems() =
        testApp { client ->
            val token = client.registerCustomer()

            // Prepare 3 meal plan days with serials 1,2,3 and attach dishes
            suspendTransaction {
                val mpId1 = MealPlanDays.insertAndGetId {
                    it[serial] = 1
                    it[current] = false
                }
                val mpId2 = MealPlanDays.insertAndGetId {
                    it[serial] = 2
                    it[current] = true // current starts at serial=2
                }
                val mpId3 = MealPlanDays.insertAndGetId {
                    it[serial] = 3
                    it[current] = false
                }
                // three dishes
                val dishId1 = Dishes.insertAndGetId {
                    it[name] = "A"
                    it[description] = "d"
                }
                val dishId2 = Dishes.insertAndGetId {
                    it[name] = "B"
                    it[description] = "d"
                }
                val dishId3 = Dishes.insertAndGetId {
                    it[name] = "C"
                    it[description] = "d"
                }
                // link: mp2 -> dishId2, mp3 -> dishId3, mp1 -> dishId1
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

            val resp = client.get("/api/customers/week") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, resp.status)
            val arr = Json.parseToJsonElement(resp.bodyAsText()).jsonArray
            assertEquals(7, arr.size)
            // Day 0 should correspond to current (serial=2) and include dish B
            val day0Dishes = arr[0].jsonObject["dishes"]!!.jsonArray
            val names0 = day0Dishes.map { it.jsonObject["dish"]!!.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names0.contains("B"))
            // Day 1 -> serial=3 has C
            val day1Dishes = arr[1].jsonObject["dishes"]!!.jsonArray
            val names1 = day1Dishes.map { it.jsonObject["dish"]!!.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names1.contains("C"))
            // Day 2 wraps to serial=1 has A
            val day2Dishes = arr[2].jsonObject["dishes"]!!.jsonArray
            val names2 = day2Dishes.map { it.jsonObject["dish"]!!.jsonObject["name"]!!.jsonPrimitive.content }
            assertTrue(names2.contains("A"))
        }
}
