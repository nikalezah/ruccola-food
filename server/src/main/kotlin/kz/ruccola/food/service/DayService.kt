package kz.ruccola.food.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDate
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.DayDishes
import kz.ruccola.food.model.Days
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Meal
import kz.ruccola.food.model.MealPlanDayDishes
import kz.ruccola.food.model.MealPlanDays
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

class DayService {
    val dishService = DishService()

    suspend fun findByDate(date: LocalDate): DayDto? =
        dbQuery {
            Days.selectAll().where { Days.date eq date }.firstOrNull()?.let(::toDto)
        }

    suspend fun copyMealPlanDishesToDate(
        mealPlanDayId: Int,
        date: LocalDate,
    ): Result<DayDto> =
        dbQuery {
            val mpdId = MealPlanDays.select(MealPlanDays.id)
                .where { MealPlanDays.id eq mealPlanDayId }
                .singleOrNull()
                ?.get(MealPlanDays.id)
                ?: return@dbQuery Result.failure(NoSuchElementException("MealPlanDay not found"))

            val dayDto = Days.selectAll().where { Days.date eq date }.firstOrNull()?.let(::toDto)
                ?: Days.insertReturning { it[Days.date] = date }.first().let(::toDto)

            // load dish ids with meals from the meal plan
            val rows = MealPlanDayDishes
                .selectAll()
                .apply { andWhere { MealPlanDayDishes.mealPlanDayId eq mpdId } }
                .toList()
            // insert triplets idempotently
            rows.forEach { row ->
                val dId = row[MealPlanDayDishes.dishId].value
                val mealStr = row[MealPlanDayDishes.meal]
                try {
                    DayDishes.insert {
                        it[DayDishes.dayId] = dayDto.id
                        it[DayDishes.dishId] = EntityID(dId, Dishes)
                        it[DayDishes.meal] = mealStr
                    }
                } catch (_: Exception) {
                }
            }
            Result.success(dayDto)
        }

    suspend fun getAllDays(language: Language): List<DayDto> =
        dbQuery {
            val days = Days.selectAll()
                .orderBy(Days.date to SortOrder.ASC)
                .map(::toDto)
                .toList()

            val dayIds = days.map { it.id }

            val dishRows = (DayDishes innerJoin Dishes).selectAll()
                .where { DayDishes.dayId inList dayIds }
                .toList()

            val dishesByDayId = mutableMapOf<Int, MutableList<DishWithMealDto>>()

            for (row in dishRows) {
                val dayId = row[DayDishes.dayId].value
                val dish = dishService.toDto(row, language)
                val mealStr = row[DayDishes.meal]
                val meal = runCatching { Meal.valueOf(mealStr) }.getOrNull()
                if (meal != null) {
                    dishesByDayId.getOrPut(dayId) { mutableListOf() }.add(DishWithMealDto(dish, meal))
                }
            }

            days.map { day ->
                day.copy(dishes = dishesByDayId[day.id] ?: emptyList())
            }
        }

    suspend fun getLatestDate(): LocalDate? =
        dbQuery {
            Days.selectAll()
                .orderBy(Days.date to SortOrder.DESC)
                .limit(1)
                .firstOrNull()
                ?.get(Days.date)
        }

    fun toDto(row: ResultRow): DayDto =
        DayDto(
            id = row[Days.id].value,
            date = row[Days.date],
        )
}
