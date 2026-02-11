package kz.ruccola.food

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DayRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testListDaysOnly() =
        testApplication {
            application { module() }
            val response = client.get("/api/days")
            assertEquals(HttpStatusCode.OK, response.status)
            // just ensure it's a JSON array
            val body = response.bodyAsText()
            // Minimal check: starts with [ and ends with ]
            assert(body.trim().startsWith("[") && body.trim().endsWith("]"))
        }

    @Test
    fun testMealPlanReorderEndpointExists() =
        testApplication {
            application { module() }
            // With no MealPlanDays in DB, the service returns success and the route should respond 200
            val response = client.post("/api/meal-plan-days/reorder") {
                contentType(ContentType.Application.Json)
                setBody("{" + "\"ids\":[]}")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
