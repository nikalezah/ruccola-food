package kz.ruccola.food.service

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishImageDto
import kz.ruccola.food.api.DishTranslation
import kz.ruccola.food.api.DishWithTranslationsDto
import kz.ruccola.food.api.PagingResponse
import kz.ruccola.food.dbQuery
import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.DishImages
import kz.ruccola.food.model.DishTranslations
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Files
import kz.ruccola.food.now
import kz.ruccola.food.service.FileService.Companion.FILES_URL_PREFIX
import kz.ruccola.food.toPagingResponse
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

class DishService {
    suspend fun exists(id: Int): Boolean =
        dbQuery {
            Dishes.selectAll().where { Dishes.id eq id }.count() > 0
        }

    suspend fun translationNameExists(
        language: Language,
        name: String,
        excludeId: Int?,
    ): Boolean =
        dbQuery {
            val baseCondition = (DishTranslations.language eq language.name) and
                (DishTranslations.name.lowerCase() eq name.lowercase())
            val condition = if (excludeId != null) {
                baseCondition and (DishTranslations.dishId neq excludeId)
            } else {
                baseCondition
            }
            DishTranslations.selectAll().where { condition }.count() > 0
        }

    suspend fun findById(
        id: Int,
        language: Language,
    ): DishDto? =
        dbQuery {
            val dish = Dishes.selectAll().where { Dishes.id eq id }.firstOrNull()
            if (dish == null) null else toDto(dish, language)
        }

    suspend fun findByIdWithTranslations(id: Int): DishWithTranslationsDto? =
        dbQuery {
            Dishes.selectAll().where { Dishes.id eq id }.singleOrNull()?.let { toDtoWithTranslations(it) }
        }

    suspend fun getAll(
        page: Int = 0,
        size: Int = 20,
        language: Language,
    ): PagingResponse<DishDto> =
        dbQuery {
            Dishes.selectAll()
                .where { Dishes.archived eq false }
                .orderBy(Dishes.id to SortOrder.ASC)
                .toPagingResponse(page, size) { toDto(it, language) }
        }

    suspend fun createDish(
        translations: Map<Language, DishTranslation>,
        imageFileIds: List<Int>,
    ): DishWithTranslationsDto =
        dbQuery {
            val dishId = Dishes.insertAndGetId {
                it[Dishes.archived] = false
                it[Dishes.createdAt] = now()
                it[Dishes.updatedAt] = now()
            }.value

            Language.entries.forEach { lang ->
                translations[lang]?.let { translation ->
                    DishTranslations.insert {
                        it[DishTranslations.dishId] = dishId
                        it[DishTranslations.language] = lang.name
                        it[DishTranslations.name] = translation.name
                        it[DishTranslations.description] = translation.description
                    }
                }
            }

            if (imageFileIds.isNotEmpty()) {
                imageFileIds.forEachIndexed { i, id ->
                    DishImages.insert {
                        it[DishImages.dishId] = dishId
                        it[DishImages.fileId] = EntityID(id, Files)
                        it[DishImages.position] = i
                        it[DishImages.createdAt] = now()
                    }
                }
            }
            toDtoWithTranslations(Dishes.selectAll().where { Dishes.id eq dishId }.single())
        }

    suspend fun updateDish(
        id: Int,
        translations: Map<Language, DishTranslation>?,
        imageFileIds: List<Int>?,
    ): DishWithTranslationsDto? =
        dbQuery {
            val dish = Dishes.selectAll().where { Dishes.id eq id }.firstOrNull()
                ?: return@dbQuery null

            Dishes.update({ Dishes.id eq id }) {
                it[Dishes.updatedAt] = now()
            }

            translations?.let { transMap ->
                DishTranslations.deleteWhere { DishTranslations.dishId eq id }
                Language.entries.forEach { lang ->
                    transMap[lang]?.let { translation ->
                        DishTranslations.insert {
                            it[DishTranslations.dishId] = id
                            it[DishTranslations.language] = lang.name
                            it[DishTranslations.name] = translation.name
                            it[DishTranslations.description] = translation.description
                        }
                    }
                }
            }

            imageFileIds?.let { newImages ->
                DishImages.deleteWhere { DishImages.dishId eq id }
                newImages.forEachIndexed { i, imageFileId ->
                    DishImages.insert {
                        it[DishImages.dishId] = id
                        it[DishImages.fileId] = EntityID(imageFileId, Files)
                        it[DishImages.position] = i
                        it[DishImages.createdAt] = now()
                    }
                }
            }

            toDtoWithTranslations(Dishes.selectAll().where { Dishes.id eq id }.single())
        }

    suspend fun archiveDish(id: Int): Boolean =
        dbQuery {
            Dishes.update({ Dishes.id eq id }) {
                it[archived] = true
                it[updatedAt] = now()
            } > 0
        }

    private suspend fun getTranslationsMap(dishId: Int): Map<Language, DishTranslation> =
        DishTranslations.selectAll()
            .where { DishTranslations.dishId eq dishId }
            .map { row ->
                val lang = Language.valueOf(row[DishTranslations.language])
                val translation = DishTranslation(
                    name = row[DishTranslations.name],
                    description = row[DishTranslations.description],
                )
                lang to translation
            }
            .toList()
            .toMap()

    suspend fun toDto(
        row: ResultRow,
        language: Language,
    ): DishDto {
        val dishId = row[Dishes.id].value
        val translation = getTranslationsMap(dishId)[language]!!
        return DishDto(
            dishId,
            translation.name,
            translation.description,
            row[Dishes.archived],
            row[Dishes.createdAt],
            row[Dishes.updatedAt],
            (DishImages innerJoin Files).selectAll()
                .where { DishImages.dishId eq dishId }
                .orderBy(DishImages.position to SortOrder.ASC, DishImages.id to SortOrder.ASC)
                .map(::toDishImageDto)
                .toList(),
        )
    }

    suspend fun toDtoWithTranslations(row: ResultRow): DishWithTranslationsDto {
        val dishId = row[Dishes.id].value
        return DishWithTranslationsDto(
            dishId,
            getTranslationsMap(dishId),
            row[Dishes.archived],
            row[Dishes.createdAt],
            row[Dishes.updatedAt],
            (DishImages innerJoin Files).selectAll()
                .where { DishImages.dishId eq dishId }
                .orderBy(DishImages.position to SortOrder.ASC, DishImages.id to SortOrder.ASC)
                .map(::toDishImageDto)
                .toList(),
        )
    }

    fun toDishImageDto(row: ResultRow): DishImageDto =
        DishImageDto(
            row[DishImages.id].value,
            "${FILES_URL_PREFIX}/${row[Files.filename]}",
            row[DishImages.fileId].value,
        )
}
