package kz.ruccola.food.service

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishImageDto
import kz.ruccola.food.api.PagingResponse
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.DishImages
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
import org.jetbrains.exposed.v1.r2dbc.updateReturning

class DishService {
    suspend fun exists(id: Int): Boolean =
        dbQuery {
            Dishes.selectAll().where { Dishes.id eq id }.count() > 0
        }

    suspend fun nameExists(
        name: String,
        excludeId: Int?,
    ): Boolean =
        dbQuery {
            val condition = (Dishes.archived eq false) and (Dishes.name.lowerCase() eq name.lowercase())
            val query = Dishes.selectAll().where {
                if (excludeId != null) condition and (Dishes.id neq excludeId) else condition
            }
            query.count() > 0
        }

    suspend fun getAll(
        page: Int = 0,
        size: Int = 20,
    ): PagingResponse<DishDto> =
        dbQuery {
            Dishes.selectAll()
                .where { Dishes.archived eq false }
                .orderBy(Dishes.id to SortOrder.ASC)
                .toPagingResponse(page, size) { toDto(it) }
        }

    suspend fun findById(id: Int): DishDto? =
        dbQuery {
            val dish = Dishes.selectAll().where { Dishes.id eq id }.firstOrNull()
            if (dish == null) null else toDto(dish)
        }

    suspend fun createDish(
        name: String,
        description: String,
        imageFileIds: List<Int>,
    ): DishDto =
        dbQuery {
            val dishId = Dishes.insertAndGetId {
                it[Dishes.name] = name
                it[Dishes.description] = description
                it[Dishes.archived] = false
                it[Dishes.createdAt] = now()
                it[Dishes.updatedAt] = now()
            }.value
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
            toDto(Dishes.selectAll().where { Dishes.id eq dishId }.single())
        }

    suspend fun updateDish(
        id: Int,
        name: String?,
        description: String?,
        imageFileIds: List<Int>?,
    ): DishDto? =
        dbQuery {
            val dishId = Dishes
                .updateReturning(where = { Dishes.id eq id }) {
                    if (name != null) it[Dishes.name] = name
                    if (description != null) it[Dishes.description] = description
                    it[Dishes.updatedAt] = now()
                }
                .singleOrNull()
                ?.let { it[Dishes.id].value }
                ?: return@dbQuery null

            imageFileIds?.let { newImages ->
                DishImages.deleteWhere { DishImages.dishId eq dishId }
                newImages.forEachIndexed { i, imageFileId ->
                    DishImages.insert {
                        it[DishImages.dishId] = dishId
                        it[DishImages.fileId] = EntityID(imageFileId, Files)
                        it[DishImages.position] = i
                        it[DishImages.createdAt] = now()
                    }
                }
            }
            toDto(Dishes.selectAll().where { Dishes.id eq dishId }.single())
        }

    suspend fun archiveDish(id: Int): Boolean =
        dbQuery {
            Dishes.update({ Dishes.id eq id }) {
                it[archived] = true
                it[updatedAt] = now()
            } > 0
        }

    suspend fun toDto(row: ResultRow): DishDto {
        val dishId = row[Dishes.id].value
        return DishDto(
            dishId,
            row[Dishes.name],
            row[Dishes.description],
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
