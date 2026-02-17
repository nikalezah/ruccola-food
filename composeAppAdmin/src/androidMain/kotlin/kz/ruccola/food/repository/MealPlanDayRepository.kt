package kz.ruccola.food.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayApi
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.model.Meal

class MealPlanDayRepository {
    private val api = MealPlanDayApi()
    private val tag = "MealPlanDayRepository"

    suspend fun getAll(): Result<List<MealPlanDayDto>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getAll())
            } catch (e: Exception) {
                Log.e(tag, "Error getting meal plan days", e)
                Result.failure(e)
            }
        }

    suspend fun save(
        id: Int?,
        dishIdToMeal: Map<Int, Meal>,
    ): Result<MealPlanDayDto> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.save(MealPlanDaySaveDto(id, dishIdToMeal)))
            } catch (e: Exception) {
                Log.e(tag, "Error updating meal plan day", e)
                Result.failure(e)
            }
        }

    suspend fun delete(id: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.delete(id))
            } catch (e: Exception) {
                Log.e(tag, "Error deleting meal plan day", e)
                Result.failure(e)
            }
        }

    suspend fun getDishes(id: Int): Result<List<DishWithMealDto>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getDishes(id))
            } catch (e: Exception) {
                Log.e(tag, "Error loading dishes with meals", e)
                Result.failure(e)
            }
        }

    suspend fun setCurrent(id: Int): Result<MealPlanDayDto> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.setCurrent(id))
            } catch (e: Exception) {
                Log.e(tag, "Error setting current meal plan day", e)
                Result.failure(e)
            }
        }

    suspend fun reorder(ids: List<Int>): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.reorder(ids))
            } catch (e: Exception) {
                Log.e(tag, "Error applying new order", e)
                Result.failure(e)
            }
        }
}
