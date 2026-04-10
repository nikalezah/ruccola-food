package kz.ruccola.food.service

import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Meal
import kz.ruccola.food.model.MealPlanDayDishes
import kz.ruccola.food.model.MealPlanDays
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.r2dbc.update

class MealPlanDayService {
    val dishService = DishService()

    suspend fun getCurrent(): MealPlanDayDto? =
        dbQuery {
            MealPlanDays.selectAll()
                .where { MealPlanDays.current eq true }
                .singleOrNull()
                ?.let { toDto(it) }
        }

    suspend fun advanceCurrentToNext() =
        dbQuery {
            val serialOfCurrent = MealPlanDays.select(MealPlanDays.serial)
                .where { MealPlanDays.current eq true }
                .single()[MealPlanDays.serial]

            val serialOfNext = MealPlanDays.select(MealPlanDays.serial)
                .where { MealPlanDays.serial eq serialOfCurrent + 1 }
                .singleOrNull()
                ?.get(MealPlanDays.serial)
                ?: 1

            MealPlanDays.update({ MealPlanDays.serial eq serialOfCurrent }) { it[MealPlanDays.current] = false }
            MealPlanDays.update({ MealPlanDays.serial eq serialOfNext }) { it[MealPlanDays.current] = true }
        }

    suspend fun getAll(language: Language): List<MealPlanDayDto> =
        dbQuery {
            val allDays = MealPlanDays.selectAll()
                .orderBy(MealPlanDays.serial to SortOrder.ASC)
                .toList()

            val dayIds = allDays.map { it[MealPlanDays.id].value }

            val dishesByDayId = if (dayIds.isNotEmpty()) {
                (MealPlanDayDishes innerJoin Dishes)
                    .selectAll()
                    .where { MealPlanDayDishes.mealPlanDayId inList dayIds }
                    .toList()
                    .groupBy { it[MealPlanDayDishes.mealPlanDayId].value }
            } else {
                emptyMap()
            }

            allDays.map { dayRow ->
                val dayId = dayRow[MealPlanDays.id].value
                val dayDishes = dishesByDayId[dayId]?.map { dishRow ->
                    DishWithMealDto(
                        dishService.toDto(dishRow, language),
                        Meal.valueOf(dishRow[MealPlanDayDishes.meal]),
                    )
                }?.sortedBy { it.meal.ordinal } ?: emptyList()
                MealPlanDayDto(
                    id = dayId,
                    serial = dayRow[MealPlanDays.serial],
                    current = dayRow[MealPlanDays.current],
                    dishes = dayDishes,
                )
            }
        }

    suspend fun save(
        id: Int?,
        dishIdToMeal: Map<Int, Meal>,
        language: Language,
    ): Result<MealPlanDayDto> =
        dbQuery {
            val mpdDto = if (id == null) {
                val max = MealPlanDays
                    .select(MealPlanDays.serial.max())
                    .singleOrNull()
                    ?.get(MealPlanDays.serial.max()) ?: 0
                MealPlanDays.insertReturning {
                    it[MealPlanDays.serial] = max + 1
                    it[MealPlanDays.current] = false
                }.single().let(::toDto)
            } else {
                val mdp = MealPlanDays.selectAll().where { MealPlanDays.id eq id }.singleOrNull()?.let(::toDto)
                    ?: return@dbQuery Result.failure(NoSuchElementException("MealPlanDay not found"))
                MealPlanDayDishes.deleteWhere { MealPlanDayDishes.mealPlanDayId eq id }
                mdp
            }
            val dishIds = dishIdToMeal.keys.toList()

            Dishes.selectAll().where { Dishes.id inList dishIds }.count() == dishIds.size.toLong() ||
                return@dbQuery Result.failure(NoSuchElementException("Dish not found"))

            val dishes = MealPlanDayDishes.batchInsert(dishIds) { dishId ->
                this[MealPlanDayDishes.mealPlanDayId] = mpdDto.id
                this[MealPlanDayDishes.dishId] = EntityID(dishId, Dishes)
                this[MealPlanDayDishes.meal] = dishIdToMeal[dishId]!!.name
            }.associate { it[MealPlanDayDishes.dishId].value to it[MealPlanDayDishes.meal] }

        /*
        val dishes = MealPlanDayDishes.select(MealPlanDayDishes.mealPlanDayId eq mpdDto.id)
            .toList()
            .map { it[MealPlanDayDishes.dishId].value to it[MealPlanDayDishes.meal]}
         */

            val dishDtoList = Dishes.selectAll().where { Dishes.id inList dishIds }
                .toList()
                .map { dishService.toDto(it, language) }

            val dishWithMeal = dishes.map { (dishId, meal) ->
                DishWithMealDto(
                    dishDtoList.first { it.id == dishId },
                    Meal.valueOf(meal),
                )
            }

            Result.success(
                MealPlanDayDto(
                    mpdDto.id,
                    mpdDto.serial,
                    mpdDto.current,
                    dishWithMeal,
                ),
            )
        }

    suspend fun delete(id: Int): Int =
        dbQuery {
            MealPlanDays.deleteWhere { MealPlanDays.id eq id }
        }

    suspend fun getDishes(
        mealPlanDayId: Int,
        language: Language,
    ): Result<List<DishWithMealDto>> =
        dbQuery {
            MealPlanDays.selectAll().where { MealPlanDays.id eq mealPlanDayId }.count() > 0 ||
                return@dbQuery Result.failure(NoSuchElementException("MealPlanDay not found"))

            val rows = MealPlanDayDishes.selectAll()
                .where { MealPlanDayDishes.mealPlanDayId eq mealPlanDayId }
                .toList()

            val dishIds = rows.map { it[MealPlanDayDishes.dishId].value }.distinct()
            val dishMap =
                if (dishIds.isEmpty()) {
                    emptyMap()
                } else {
                    Dishes.selectAll().where { Dishes.id inList dishIds }
                        .toList()
                        .map { dishService.toDto(it, language) }
                        .associateBy { it.id }
                }

            val list = rows.mapNotNull { row ->
                val dishIdVal = row[MealPlanDayDishes.dishId].value
                val dish = dishMap[dishIdVal]
                val mealStr = row[MealPlanDayDishes.meal]
                val meal = runCatching { Meal.valueOf(mealStr) }.getOrElse { null }
                if (dish != null && meal != null) DishWithMealDto(dish, meal) else null
            }
                .sortedBy { it.meal.ordinal }

            Result.success(list)
        }

    suspend fun setCurrent(id: Int): Result<MealPlanDayDto> =
        dbQuery {
            MealPlanDays.selectAll().where { MealPlanDays.id eq id }.count() > 0 ||
                return@dbQuery Result.failure(NoSuchElementException("MealPlanDay not found"))

            MealPlanDays.update({ MealPlanDays.current eq true }) { it[MealPlanDays.current] = false }
            MealPlanDays.update({ MealPlanDays.id eq id }) { it[MealPlanDays.current] = true }

            Result.success(
                // todo: don't return whole object, return only true/false?
                MealPlanDays.selectAll().where { MealPlanDays.id eq id }.single().let(::toDto),
            )
        }

    suspend fun reorder(ids: List<Int>): Result<Boolean> =
        dbQuery {
            val entityIds = MealPlanDays.select(MealPlanDays.id).toList().map { it[MealPlanDays.id].value }
            if (entityIds.isEmpty()) return@dbQuery Result.success(true)
            val reqSet = ids.toSet()
            if (ids.size < 2 || reqSet.size != ids.size) {
                return@dbQuery Result.failure(
                    IllegalArgumentException("ids must be a permutation of existing MealPlanDay ids"),
                )
            }
            if (reqSet != entityIds.toSet()) {
                return@dbQuery Result.failure(NoSuchElementException("Not found"))
            }
            // Use raw SQL to avoid any chance of unique constraint violation during intermediate steps.
            // Step 1: shift all serials by a large offset in a single statement (no duplicates possible).
            val offset = 1000000
            TransactionManager.current().exec("UPDATE meal_plan_days SET serial = serial + $offset;")
            // Step 2: assign final 1..n order for the provided ids using a CASE expression.
            val caseClauses = ids.withIndex().joinToString(" ") { (idx, id) -> "WHEN $id THEN ${idx + 1}" }
            val idList = ids.joinToString(",")
            val updateSql = "UPDATE meal_plan_days SET serial = CASE id $caseClauses END WHERE id IN ($idList);"
            TransactionManager.current().exec(updateSql)
            Result.success(true)
        }

    fun toDto(row: ResultRow): MealPlanDayDto =
        MealPlanDayDto(
            row[MealPlanDays.id].value,
            row[MealPlanDays.serial],
            row[MealPlanDays.current],
        )
}
