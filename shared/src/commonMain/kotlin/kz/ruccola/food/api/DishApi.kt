package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kz.ruccola.food.localization.Language

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
    }
}

class DishApi(
    private val client: HttpClient = httpClient,
) {
    suspend fun getDishById(id: Int): DishDto = client.get(Dishes.Id(id = id)).body()

    suspend fun getAllDishesWithTranslations(
        page: Int = 0,
        size: Int = 20,
    ): PagingResponse<DishWithTranslationsDto> = client.get(Dishes.List(page = page, size = size)).body()

    suspend fun getDishByIdWithTranslations(id: Int): DishWithTranslationsDto = client.get(Dishes.Id(id = id)).body()

    suspend fun createDish(newDish: DishCreateDto): DishWithTranslationsDto =
        client.post(Dishes()) {
            contentType(ContentType.Application.Json)
            setBody(newDish)
        }.body()

    suspend fun updateDish(
        id: Int,
        updateDish: DishUpdateDto,
    ): DishWithTranslationsDto =
        client.put(Dishes.Id(id = id)) {
            contentType(ContentType.Application.Json)
            setBody(updateDish)
        }.body()

    suspend fun archiveDish(id: Int): Boolean =
        client.post(Dishes.Id.Archive(parent = Dishes.Id(id = id))).status.isSuccess()
}

@Serializable
data class DishTranslation(
    val name: String,
    val description: String,
)

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
    val translations: Map<Language, DishTranslation>,
    val imageFileIds: List<Int> = emptyList(),
)

@Serializable
data class DishUpdateDto(
    val translations: Map<Language, DishTranslation>? = null,
    val imageFileIds: List<Int>? = null,
)

@Serializable
data class DishWithTranslationsDto(
    val id: Int,
    val translations: Map<Language, DishTranslation>,
    val archived: Boolean,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeIso8601Serializer::class)
    val updatedAt: LocalDateTime,
    val images: List<DishImageDto> = emptyList(),
)

@Serializable
data class DishImageDto(
    val id: Int,
    val url: String,
    val fileId: Int,
)
