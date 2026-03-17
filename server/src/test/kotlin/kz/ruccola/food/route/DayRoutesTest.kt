package kz.ruccola.food.route

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDate
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.api.MealPlanDaysReorderDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.dbQuery
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.model.DayDishes
import kz.ruccola.food.model.Days
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Meal
import kz.ruccola.food.testApp
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DayRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testListDaysOnly() =
        testApp { client ->
            val token = client.loginAdmin()

            // add a day with a dish manually for testing
            dbQuery {
                val dishId = Dishes.insertReturning {
                    it[name] = "Test Dish"
                    it[description] = "Desc"
                }.toList().first()[Dishes.id]

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
            assertEquals("Test Dish", firstDay.dishes.first().dish.name)
            assertEquals(Meal.BREAKFAST, firstDay.dishes.first().meal)
        }

    @Test
    fun testMealPlanReorderEndpointExists() =
        testApp { client ->
            val token = client.loginAdmin()
            // With no MealPlanDays in DB, the service returns success and the route should respond 200
            val response = client.post("/api/meal-plan-days/reorder") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(emptyList()))
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
