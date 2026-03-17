package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.serialization.Serializable

@Resource("customers")
class Customers {
    @Resource("profile")
    class Profile(
        val parent: Customers = Customers(),
    )

    @Resource("plan")
    class Plan(
        val parent: Customers = Customers(),
    )

    @Resource("schedule")
    class Schedule(
        val parent: Customers = Customers(),
    )
}

class CustomerApi(
    private val client: HttpClient = httpClient,
) {
    suspend fun getAll(): List<CustomerDto> = client.get(Customers()).body()

    suspend fun get(): CustomerDto {
        val response = client.get(Customers.Profile())
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun update(payload: CustomerUpdateDto): CustomerDto {
        val response = client.put(Customers.Profile()) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun getCustomerPlan(): CustomerPlanDetailsDto? {
        val response = client.get(Customers.Plan())
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun saveCustomerPlan(newCustomerPlan: CustomerPlanCreateDto): CustomerPlanDetailsDto {
        val response = client.post(Customers.Plan()) {
            contentType(ContentType.Application.Json)
            setBody(newCustomerPlan)
        }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun getSchedule(): List<ScheduledDayDto> = client.get(Customers.Schedule()).body()
}

@Serializable
data class CustomerDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val role: String,
    val calories: Int? = null,
    val lastMessage: String? = null,
)

@Serializable
data class CustomerUpdateDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val address: String? = null,
)

@Serializable
data class CustomerPlanDetailsDto(
    val id: Int,
    val customerId: Int,
    val plan: PlanDto,
    @Serializable(with = LocalDateIso8601Serializer::class)
    val chosenDate: LocalDate,
)

@Serializable
data class CustomerPlanCreateDto(
    val planId: Int,
    @Serializable(with = LocalDateIso8601Serializer::class)
    val chosenDate: LocalDate,
)

@Serializable
data class ScheduledDayDto(
    val date: LocalDate,
    val dishes: List<DishWithMealDto>,
)
