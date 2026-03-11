package kz.ruccola.food.route

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kz.ruccola.food.api.MealPlanDaysReorderDto
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.testApp
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MealPlanDayReorderRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testReorderEndpointSmoke() =
        testApp { client ->
            val token = client.loginAdmin()
            val response = client.post("/api/meal-plan-days/reorder") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(emptyList()))
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
