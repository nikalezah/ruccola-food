package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.Serializable

@Resource("dishes")
class Dishes {
    @Resource("")
    class List(
        val parent: Dishes = Dishes(),
        val page: Int = 0,
        val size: Int = 20,
    )

    @Resource("{id}")
    class Id(
        val parent: Dishes = Dishes(),
        val id: Int,
    ) {
        @Resource("archive")
        class Archive(
            val parent: Id,
        )

        @Resource("variants")
        class Variants(
            val parent: Dishes.Id,
        ) {
            @Resource("{variantId}")
            class Id(
                val parent: Variants,
                val variantId: Int,
            ) {
                @Resource("customers")
                class Customers(
                    val parent: Id,
                )
            }
        }
    }
}

class DishApi(
    private val client: HttpClient = httpClient,
) {
    suspend fun getAllDishes(
        page: Int = 0,
        size: Int = 20,
    ): PagingResponse<DishDto> = client.get(Dishes.List(page = page, size = size)).body()

    suspend fun getDishById(id: Int): DishDto = client.get(Dishes.Id(id = id)).body()

    suspend fun createDish(newDish: DishCreateDto): DishDto =
        client.post(Dishes()) {
            contentType(ContentType.Application.Json)
            setBody(newDish)
        }.body()

    suspend fun updateDish(
        id: Int,
        updateDish: DishUpdateDto,
    ): DishDto =
        client.put(Dishes.Id(id = id)) {
            contentType(ContentType.Application.Json)
            setBody(updateDish)
        }.body()

    suspend fun archiveDish(id: Int): Boolean =
        client.post(Dishes.Id.Archive(parent = Dishes.Id(id = id))).status.isSuccess()

    suspend fun getVariants(dishId: Int): List<DishVariantDto> =
        client.get(Dishes.Id.Variants(parent = Dishes.Id(id = dishId))).body()

    suspend fun createVariant(
        dishId: Int,
        payload: DishVariantSaveDto,
    ): DishVariantDto =
        client.post(Dishes.Id.Variants(parent = Dishes.Id(id = dishId))) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()

    suspend fun updateVariant(
        dishId: Int,
        variantId: Int,
        payload: DishVariantSaveDto,
    ): DishVariantDto =
        client.put(
            Dishes.Id.Variants.Id(
                parent = Dishes.Id.Variants(parent = Dishes.Id(id = dishId)),
                variantId = variantId,
            ),
        ) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()

    suspend fun deleteVariant(
        dishId: Int,
        variantId: Int,
    ): Boolean =
        client.delete(
            Dishes.Id.Variants.Id(
                parent = Dishes.Id.Variants(parent = Dishes.Id(id = dishId)),
                variantId = variantId,
            ),
        ).status.isSuccess()

    suspend fun getVariantCustomers(
        dishId: Int,
        variantId: Int,
    ): List<Int> =
        client.get(
            Dishes.Id.Variants.Id.Customers(
                parent = Dishes.Id.Variants.Id(
                    parent = Dishes.Id.Variants(parent = Dishes.Id(id = dishId)),
                    variantId = variantId,
                ),
            ),
        ).body()

    suspend fun setVariantCustomers(
        dishId: Int,
        variantId: Int,
        ids: List<Int>,
    ): Boolean =
        client.put(
            Dishes.Id.Variants.Id.Customers(
                parent = Dishes.Id.Variants.Id(
                    parent = Dishes.Id.Variants(parent = Dishes.Id(id = dishId)),
                    variantId = variantId,
                ),
            ),
        ) {
            contentType(ContentType.Application.Json)
            setBody(VariantCustomersPayload(ids))
        }.status.isSuccess()
}

@Serializable
data class DishDto(
    val id: Int,
    val name: String,
    val description: String,
    val archived: Boolean,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val updatedAt: LocalDateTime,
    val images: List<DishImageDto> = emptyList(),
)

@Serializable
data class DishCreateDto(
    val name: String,
    val description: String,
    val imageFileIds: List<Int> = emptyList(),
)

@Serializable
data class DishUpdateDto(
    val name: String? = null,
    val description: String? = null,
    val imageFileIds: List<Int>? = null,
)

@Serializable
data class DishImageDto(
    val id: Int,
    val url: String,
    val fileId: Int,
)

@Serializable
data class DishVariantDto(
    val id: Int,
    val dishId: Int,
    val description: String,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val updatedAt: LocalDateTime,
    val customerIds: List<Int> = emptyList(),
)

@Serializable
data class DishVariantSaveDto(
    val description: String,
)

@Serializable
data class VariantCustomersPayload(
    val customerIds: List<Int>,
)
