package kz.ruccola.food

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
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
        testApplication {
            application { module() }
            val response = client.post("/api/meal-plan-days/reorder") {
                contentType(ContentType.Application.Json)
                setBody("{" + "\"ids\":[]}")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
