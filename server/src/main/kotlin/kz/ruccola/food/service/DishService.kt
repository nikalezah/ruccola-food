package kz.ruccola.food.service

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishImageDto
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.DishImages
import kz.ruccola.food.model.DishVariantCustomers
import kz.ruccola.food.model.DishVariants
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Files
import kz.ruccola.food.now
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.jetbrains.exposed.v1.r2dbc.updateReturning

class DishService {
    suspend fun getVariantCustomerIds(
        dishId: Int,
        variantId: Int,
    ): List<Int> =
        dbQuery {
            val variantDishId = DishVariants.select(DishVariants.dishId)
                .where { DishVariants.id eq variantId }
                .singleOrNull()
                ?.get(DishVariants.dishId)
                ?.value
                ?: return@dbQuery listOf()

            if (variantDishId != dishId) return@dbQuery listOf()

            DishVariantCustomers.selectAll()
                .where { DishVariantCustomers.variantId eq variantId }
                .map { it[DishVariantCustomers.customerId].value }
                .toList()
        }

    suspend fun setVariantCustomerIds(
        dishId: Int,
        variantId: Int,
        customerIds: List<Int>,
    ): Boolean =
        dbQuery {
            val variantDishId = DishVariants.select(DishVariants.dishId)
                .where { DishVariants.id eq variantId }
                .singleOrNull()
                ?.get(DishVariants.dishId)
                ?.value
                ?: return@dbQuery false

            if (variantDishId != dishId) return@dbQuery false
            // Clear existing
            suspendTransaction {
                DishVariantCustomers.deleteWhere {
                    (DishVariantCustomers.variantId eq variantId) or
                        (DishVariantCustomers.customerId inList customerIds)
                }
            }
            // Insert new
            suspendTransaction {
                DishVariantCustomers.batchInsert(customerIds) { customerId ->
                    this[DishVariantCustomers.variantId] = variantId
                    this[DishVariantCustomers.customerId] = customerId
                }
            }
            true
        }

    suspend fun getDishVariants(dishId: Int): List<DishVariantDto> =
        dbQuery {
            DishVariants.selectAll().where { DishVariants.dishId eq dishId }
                .orderBy(DishVariants.id to SortOrder.ASC)
                .map(::toDishVariantDto)
                .toList()
        }

    suspend fun addDishVariant(
        dishId: Int,
        description: String,
    ): DishVariantDto? =
        dbQuery {
            val dId = findById(dishId)?.id ?: return@dbQuery null
            DishVariants.insertReturning {
                it[this.dishId] = dId
                it[this.description] = description
                it[this.createdAt] = now()
                it[this.updatedAt] = now()
            }.firstOrNull()?.let(::toDishVariantDto)
        }

    suspend fun updateDishVariant(
        dishId: Int,
        variantId: Int,
        description: String,
    ): DishVariantDto? =
        dbQuery {
            DishVariants.updateReturning(
                where = { (DishVariants.id eq variantId) and (DishVariants.dishId eq dishId) },
            ) {
                it[DishVariants.description] = description
                it[DishVariants.updatedAt] = now()
            }
                .singleOrNull()
                ?.let(::toDishVariantDto)
        }

    suspend fun deleteDishVariant(
        dishId: Int,
        variantId: Int,
    ): Boolean =
        dbQuery {
            val variantDishId = DishVariants.select(DishVariants.dishId)
                .where { DishVariants.id eq variantId }
                .singleOrNull()
                ?.get(DishVariants.dishId)
                ?.value
                ?: return@dbQuery false

            if (variantDishId != dishId) return@dbQuery false

            DishVariants.deleteWhere { DishVariants.id eq variantId }
            true
        }

    suspend fun getAll(): List<DishDto> =
        dbQuery {
            Dishes.selectAll().where { Dishes.archived eq false }
                .orderBy(Dishes.id to SortOrder.ASC)
                .toList()
                .map { toDto(it) }
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
            "/${FileService.FILES_DIR_PATH}/${row[Files.filename]}",
            row[DishImages.fileId].value,
        )

    fun toDishVariantDto(row: ResultRow): DishVariantDto =
        DishVariantDto(
            row[DishVariants.id].value,
            row[DishVariants.dishId].value,
            row[DishVariants.description],
            row[DishVariants.createdAt],
            row[DishVariants.updatedAt],
        )
}
