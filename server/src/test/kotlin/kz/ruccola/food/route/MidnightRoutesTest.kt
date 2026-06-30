package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.Days
import kz.ruccola.food.model.Meal
import kz.ruccola.food.model.MealPlanDayDishes
import kz.ruccola.food.model.MealPlanDays
import kz.ruccola.food.seedDish
import kz.ruccola.food.testApp
import kz.ruccola.food.today
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MidnightRoutesTest : RouteIntegrationTest() {
    @Test
    fun midnightCreatesDayFromCurrentMealPlanAndAdvances() =
        testApp { client ->
            val token = client.loginAdmin()
            val dishId = seedDish("Midnight Pasta", nameRu = "Паста")

            suspendTransaction {
                val mpd1 = MealPlanDays.insertAndGetId {
                    it[serial] = 1
                    it[current] = true
                }
                MealPlanDays.insertAndGetId {
                    it[serial] = 2
                    it[current] = false
                }
                MealPlanDayDishes.insert {
                    it[MealPlanDayDishes.mealPlanDayId] = mpd1
                    it[MealPlanDayDishes.dishId] = dishId
                    it[MealPlanDayDishes.meal] = Meal.BREAKFAST.name
                }
            }

            val response = client.post("/api/days/midnight") { authHeader(token) }
            assertEquals(HttpStatusCode.OK, response.status)

            val days = client.get("/api/days") { authHeader(token) }.body<List<DayDto>>()
            assertEquals(1, days.size)
            assertEquals(today(), days.first().date)
            assertEquals(1, days.first().dishes.size)
            assertEquals("Паста", days.first().dishes.first().dish.name)

            val currentMpd = suspendTransaction {
                MealPlanDays.selectAll().where { MealPlanDays.current eq true }.singleOrNull()
            }
            assertNotNull(currentMpd)
            assertEquals(2, currentMpd[MealPlanDays.serial])
        }

    @Test
    fun midnightIsIdempotentWhenDayAlreadyExists() =
        testApp { client ->
            val token = client.loginAdmin()
            val dishId = seedDish("Idempotent Dish", nameRu = "Блюдо")

            val tomorrow = today().plus(1, DateTimeUnit.DAY)

            suspendTransaction {
                val mpdId = MealPlanDays.insertAndGetId {
                    it[serial] = 1
                    it[current] = true
                }
                MealPlanDayDishes.insert {
                    it[MealPlanDayDishes.mealPlanDayId] = mpdId
                    it[MealPlanDayDishes.dishId] = dishId
                    it[MealPlanDayDishes.meal] = Meal.LAUNCH.name
                }
                Days.insert {
                    it[Days.date] = today()
                }
                Days.insert {
                    it[Days.date] = tomorrow
                }
            }

            client.post("/api/days/midnight") { authHeader(token) }
                .apply { assertEquals(HttpStatusCode.OK, status) }

            val daysAfter = client.get("/api/days") { authHeader(token) }.body<List<DayDto>>()
            val tomorrowDays = daysAfter.filter { it.date == tomorrow }
            assertEquals(1, tomorrowDays.size)
            assertTrue(tomorrowDays.first().dishes.isEmpty())
        }
}
