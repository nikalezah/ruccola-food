package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.resources.Resource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.serialization.Serializable

@Resource("days")
class Days {
    // Dev/testing endpoint that triggers midnight functions
    @Resource("midnight")
    class Midnight(val parent: Days = Days())
}

class DayApi(private val client: HttpClient = httpClient) {
    suspend fun getAllDays(): List<DayDto> = client.get(Days()).body()

    suspend fun triggerMidnight(): String {
        // Ignore the provided currentDate; server computes the next date internally
        return client.post(Days.Midnight()).body()
    }
}

@Serializable
data class DayDto(
    val id: Int,
    @Serializable(with = LocalDateIso8601Serializer::class) val date: LocalDate,
    val dishes: List<DishWithMealDto> = emptyList(),
)
