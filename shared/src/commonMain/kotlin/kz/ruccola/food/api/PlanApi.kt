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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.Serializable
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays

@Resource("plans")
class Plans {
    @Resource("{id}")
    class Id(
        val parent: Plans = Plans(),
        val id: Int,
    )

    @Resource("calories")
    class Calories(
        val parent: Plans = Plans(),
    )

    @Resource("days")
    class Days(
        val parent: Plans = Plans(),
        val calories: Int,
    )
}

class PlanApi(
    private val client: HttpClient = httpClient,
) {
    suspend fun getAll(): List<PlanDto> = client.get(Plans()).body()

    suspend fun create(payload: PlanCreateDto): PlanDto {
        val resp = client.post(Plans()) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        if (!resp.status.isSuccess()) throw Exception(resp.bodyAsText())
        return resp.body()
    }

    suspend fun update(
        id: Int,
        payload: PlanUpdateDto,
    ): PlanDto {
        val resp = client.put(Plans.Id(id = id)) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        if (!resp.status.isSuccess()) throw Exception(resp.bodyAsText())
        return resp.body()
    }

    suspend fun delete(id: Int): Boolean = client.delete(Plans.Id(id = id)).status.isSuccess()

    suspend fun getAvailableCalories(): List<Int> = client.get(Plans.Calories()).body()

    suspend fun getAvailableDays(calories: Int): List<Int> = client.get(Plans.Days(calories = calories)).body()
}

@Serializable
data class PlanDto(
    val id: Int,
    val calories: PlanCalories,
    val periodDays: PlanDays,
    val pricePerDay: Int,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val updatedAt: LocalDateTime,
)

@Serializable
data class PlanCreateDto(
    val calories: PlanCalories,
    val periodDays: PlanDays,
    val pricePerDay: Int,
)

@Serializable
data class PlanUpdateDto(
    val calories: PlanCalories? = null,
    val periodDays: PlanDays? = null,
    val pricePerDay: Int? = null,
)
