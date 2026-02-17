package kz.ruccola.food.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.ruccola.food.api.DayApi
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.api.DishDto

class DayRepository {
    private val dayApi = DayApi()
    private val tag = "DayRepository"

    suspend fun getAllDays(): Result<List<DayDto>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Getting all days")
                val days = dayApi.getAllDays()
                Result.success(days)
            } catch (e: Exception) {
                Log.e(tag, "Error getting days", e)
                Result.failure(e)
            }
        }

    suspend fun getDishes(dayId: Int): Result<List<DishDto>> =
        withContext(Dispatchers.IO) {
            try {
                val list = dayApi.getDishes(dayId)
                Result.success(list.map { it.dish })
            } catch (e: Exception) {
                Log.e(tag, "Error loading day dishes", e)
                Result.failure(e)
            }
        }

    suspend fun triggerMidnight(): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val msg = dayApi.triggerMidnight()
                Result.success(msg)
            } catch (e: Exception) {
                Log.e(tag, "Error triggering midnight", e)
                Result.failure(e)
            }
        }
}
