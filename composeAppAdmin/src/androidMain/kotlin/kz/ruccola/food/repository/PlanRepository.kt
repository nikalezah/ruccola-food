package kz.ruccola.food.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.ruccola.food.api.PlanApi
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays

class PlanRepository {
    private val api = PlanApi()
    private val tag = "PlanRepository"

    suspend fun getAll(): Result<List<PlanDto>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getAll())
            } catch (e: Exception) {
                Log.e(tag, "Error getting plans", e)
                Result.failure(e)
            }
        }

    suspend fun create(
        calories: PlanCalories,
        periodDays: PlanDays,
        pricePerDay: Int,
        allowVariantChoice: Boolean,
    ): Result<PlanDto> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.create(PlanCreateDto(calories, periodDays, pricePerDay, allowVariantChoice)))
            } catch (e: Exception) {
                Log.e(tag, "Error creating plan", e)
                Result.failure(e)
            }
        }

    suspend fun update(
        id: Int,
        calories: PlanCalories?,
        periodDays: PlanDays?,
        pricePerDay: Int?,
        allowVariantChoice: Boolean?,
    ): Result<PlanDto> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.update(id, PlanUpdateDto(calories, periodDays, pricePerDay, allowVariantChoice)))
            } catch (e: Exception) {
                Log.e(tag, "Error updating plan", e)
                Result.failure(e)
            }
        }

    suspend fun delete(id: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.delete(id))
            } catch (e: Exception) {
                Log.e(tag, "Error deleting plan", e)
                Result.failure(e)
            }
        }
}
