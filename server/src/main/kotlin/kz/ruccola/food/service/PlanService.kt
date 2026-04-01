package kz.ruccola.food.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.model.Plans
import kz.ruccola.food.now
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.updateReturning

class PlanService {
    suspend fun getAll(): List<PlanDto> =
        dbQuery {
            Plans.selectAll().orderBy(Plans.id to SortOrder.ASC).map(::toDto).toList()
        }

    suspend fun exists(
        calories: Int,
        periodDays: Int,
    ): Boolean =
        dbQuery {
            Plans.selectAll()
                .where { (Plans.calories eq calories) and (Plans.periodDays eq periodDays) }
                .count() > 0
        }

    suspend fun create(newPlan: PlanCreateDto): PlanDto =
        dbQuery {
            Plans.insertReturning {
                it[calories] = newPlan.calories.value
                it[periodDays] = newPlan.periodDays.amount
                it[pricePerDay] = newPlan.pricePerDay
                it[createdAt] = now()
                it[updatedAt] = now()
            }.single().let(::toDto)
        }

    suspend fun update(
        id: Int,
        update: PlanUpdateDto,
    ): PlanDto? =
        dbQuery {
            Plans.updateReturning(where = { Plans.id eq id }) {
                update.pricePerDay?.let { p -> it[pricePerDay] = p }
                it[updatedAt] = now()
            }
                .singleOrNull()
                ?.let(::toDto)
                ?: return@dbQuery null
        }

    suspend fun delete(id: Int): Int =
        dbQuery {
            Plans.deleteWhere { Plans.id eq id }
        }

    fun toDto(row: ResultRow): PlanDto =
        PlanDto(
            row[Plans.id].value,
            PlanCalories.fromValue(row[Plans.calories]),
            PlanDays.fromDays(row[Plans.periodDays]),
            row[Plans.pricePerDay],
            row[Plans.createdAt],
            row[Plans.updatedAt],
        )
}
