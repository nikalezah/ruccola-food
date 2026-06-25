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

    @Resource("prefs")
    class Prefs(
        val parent: Customers = Customers(),
    )

    @Resource("schedule")
    class Schedule(
        val parent: Customers = Customers(),
        val page: Int = 0,
        val size: Int = 20,
    )
}

class CustomerApi(
    private val client: HttpClient = httpClient,
) {
    suspend fun getAll(): List<CustomerDetailsDto> = client.get(Customers()).body()

    suspend fun get(): CustomerDto {
        val response = client.get(Customers.Profile())
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun getPlanWithPrefs(): CustomerPlanWithPrefsDto {
        val response = client.get(Customers.Plan())
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

    suspend fun getSchedule(
        page: Int = 0,
        size: Int = 20,
    ): PagingResponse<ScheduledDayDto> = client.get(Customers.Schedule(page = page, size = size)).body()

    suspend fun saveDeliveryPrefs(prefs: CustomerPrefsUpdateDto): CustomerPrefsDto {
        val response = client.put(Customers.Prefs()) {
            contentType(ContentType.Application.Json)
            setBody(prefs)
        }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }
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
data class CustomerDetailsDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val role: String,
    val prefs: CustomerPrefsDto,
    val plan: CustomerPlanDetailsDto? = null,
    val lastMessage: String? = null,
)

@Serializable
data class CustomerUpdateDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val address: String? = null,
)

@Serializable
data class CustomerPlanWithPrefsDto(
    val plan: CustomerPlanDetailsDto,
    val prefs: CustomerPrefsDto,
)

@Serializable
data class CustomerPlanDetailsDto(
    val id: Int,
    val customerId: Int,
    val calories: Int,
    val pricePerDay: Int,
    val days: Int,
    @Serializable(with = LocalDateIso8601Serializer::class)
    val chosenDate: LocalDate,
)

@Serializable
data class CustomerPlanCreateDto(
    val planId: Int,
    val days: Int,
    @Serializable(with = LocalDateIso8601Serializer::class)
    val chosenDate: LocalDate,
)

@Serializable
data class ScheduledDayDto(
    val date: LocalDate,
    val dishes: List<DishWithMealDto>,
)

@Serializable
data class CustomerPrefsDto(
    val needsCutlery: Boolean,
    val weekendDelivery: Boolean,
    val morningDelivery: Boolean,
)

@Serializable
data class CustomerPrefsUpdateDto(
    val needsCutlery: Boolean? = null,
    val weekendDelivery: Boolean? = null,
    val morningDelivery: Boolean? = null,
)
