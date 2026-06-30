package kz.ruccola.food.route

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kz.ruccola.food.RouteIntegrationTest
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishTranslation
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.authHeader
import kz.ruccola.food.localization.Language
import kz.ruccola.food.registerCustomer
import kz.ruccola.food.testApp
import kotlin.test.Test
import kotlin.test.assertEquals

class RoleAccessTest : RouteIntegrationTest() {
    @Test
    fun customerForbiddenOnAdminEndpoints() =
        testApp { client ->
            val token = client.registerCustomer().token

            val cases = listOf(
                "POST" to "/api/dishes",
                "POST" to "/api/files",
                "PUT" to "/api/meal-plan-days",
                "POST" to "/api/plans",
                "POST" to "/api/days/midnight",
            )

            cases.forEach { (method, path) ->
                val response = when (method) {
                    "POST" -> client.post(path) { authHeader(token) }
                    "PUT" -> client.put(path) { authHeader(token) }
                    else -> error("Unsupported method: $method")
                }
                assertEquals(HttpStatusCode.Forbidden, response.status, "$method $path")
            }
        }

    @Test
    fun customerForbiddenOnDishCreateWithBody() =
        testApp { client ->
            val token = client.registerCustomer().token
            val response = client.post("/api/dishes") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(
                    DishCreateDto(
                        translations = mapOf(
                            Language.EN to DishTranslation("Soup", "Hot soup"),
                            Language.RU to DishTranslation("Суп", "Горячий суп"),
                            Language.KK to DishTranslation("Сорпа", "Ыстық сорпа"),
                        ),
                    ),
                )
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun customerForbiddenOnMealPlanDaySave() =
        testApp { client ->
            val token = client.registerCustomer().token
            val response = client.put("/api/meal-plan-days") {
                authHeader(token)
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaySaveDto(null, emptyMap()))
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun customerCanListPlans() =
        testApp { client ->
            val token = client.registerCustomer().token
            val response = client.get("/api/plans") { authHeader(token) }
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
