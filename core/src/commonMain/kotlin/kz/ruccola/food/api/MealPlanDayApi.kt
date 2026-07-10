package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import kz.ruccola.food.model.Meal

@Resource("meal-plan-days")
class MealPlanDays {
    @Resource("{id}")
    class Id(val parent: MealPlanDays = MealPlanDays(), val id: Int) {
        @Resource("dishes")
        class Dishes(val parent: Id)

        @Resource("current")
        class Current(val parent: Id)
    }

    @Resource("reorder")
    class Reorder(val parent: MealPlanDays = MealPlanDays())
}

class MealPlanDayApi(private val client: HttpClient = httpClient) {
    suspend fun getAll(): List<MealPlanDayDto> = client.get(MealPlanDays()).body()

    suspend fun save(save: MealPlanDaySaveDto): MealPlanDayDto {
        val response =
            client.put(MealPlanDays()) {
                contentType(ContentType.Application.Json)
                setBody(save)
            }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull() ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun delete(id: Int): Boolean = client.delete(MealPlanDays.Id(id = id)).status.isSuccess()

    suspend fun setCurrent(id: Int): MealPlanDayDto {
        val response = client.post(MealPlanDays.Id.Current(parent = MealPlanDays.Id(id = id)))
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull() ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun reorder(ids: List<Int>): Boolean {
        val response =
            client.post(MealPlanDays.Reorder()) {
                contentType(ContentType.Application.Json)
                setBody(MealPlanDaysReorderDto(ids))
            }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull() ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return true
    }
}

@Serializable
data class MealPlanDayDto(
    val id: Int,
    val serial: Int,
    val current: Boolean = false,
    val dishes: List<DishWithMealDto> = listOf(),
)

@Serializable
data class MealPlanDaySaveDto(val id: Int?, val dishIdToMeal: Map<Int, Meal>)

@Serializable
data class MealPlanDaysReorderDto(val ids: List<Int>)
