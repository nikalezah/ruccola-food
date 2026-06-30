package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDate
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.dbQuery
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.DayDishes
import kz.ruccola.food.model.Days
import kz.ruccola.food.model.Meal
import kz.ruccola.food.seedDish
import kz.ruccola.food.testApp
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DayRoutesTest : RouteIntegrationTest() {
    @Test
    fun testListDaysOnly() =
        testApp { client ->
            val token = client.loginAdmin()
            val dishId = seedDish("Test Dish", nameRu = "Тестовое блюдо")

            dbQuery {
                val dayId = Days.insertReturning {
                    it[date] = LocalDate(2025, 1, 1)
                }.toList().first()[Days.id]

                DayDishes.insert {
                    it[DayDishes.dayId] = dayId
                    it[DayDishes.dishId] = dishId
                    it[meal] = Meal.BREAKFAST.name
                }
            }

            val response = client.get("/api/days") { authHeader(token) }
            assertEquals(HttpStatusCode.OK, response.status)

            val days = response.body<List<DayDto>>()
            assertTrue(days.isNotEmpty())
            val firstDay = days.first()
            assertEquals(1, firstDay.dishes.size)
            assertEquals("Тестовое блюдо", firstDay.dishes.first().dish.name)
            assertEquals(Meal.BREAKFAST, firstDay.dishes.first().meal)
        }
}
