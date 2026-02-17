package kz.ruccola.food.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.api.DishVariantSaveDto

/**
 * Repository class for handling dish operations on Android
 */
class DishRepository {
    // On Android emulator, use 10.0.2.2 to access the host machine (instead of localhost)
    private val dishApi = DishApi()
    private val tag = "DishRepository"

    // Variants
    suspend fun getVariants(dishId: Int): Result<List<DishVariantDto>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(dishApi.getVariants(dishId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun addVariant(
        dishId: Int,
        description: String,
    ): Result<DishVariantDto> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(dishApi.createVariant(dishId, DishVariantSaveDto(description)))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateVariant(
        dishId: Int,
        variantId: Int,
        description: String,
    ): Result<DishVariantDto> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(dishApi.updateVariant(dishId, variantId, DishVariantSaveDto(description)))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteVariant(
        dishId: Int,
        variantId: Int,
    ): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(dishApi.deleteVariant(dishId, variantId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getVariantCustomers(
        dishId: Int,
        variantId: Int,
    ): Result<List<Int>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(dishApi.getVariantCustomers(dishId, variantId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun setVariantCustomers(
        dishId: Int,
        variantId: Int,
        ids: List<Int>,
    ): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(dishApi.setVariantCustomers(dishId, variantId, ids))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Get all dishes with Android-specific error handling
     */
    suspend fun getAllDishes(): Result<List<DishDto>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Getting all dishes")
                val dishes = dishApi.getAllDishes()
                Log.d(tag, "Successfully retrieved ${dishes.size} dishes")
                Result.success(dishes)
            } catch (e: Exception) {
                Log.e(tag, "Error getting all dishes", e)
                Result.failure(e)
            }
        }

    /**
     * Get a dish by ID with Android-specific error handling
     */
    suspend fun getDishById(id: Int): Result<DishDto> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Getting dish with ID: $id")
                val dish = dishApi.getDishById(id)
                Log.d(tag, "Successfully retrieved dish: ${dish.name}")
                Result.success(dish)
            } catch (e: Exception) {
                Log.e(tag, "Error getting dish with ID: $id", e)
                Result.failure(e)
            }
        }

    /**
     * Create a new dish with Android-specific error handling
     */
    suspend fun createDish(
        name: String,
        description: String,
    ): Result<DishDto> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Creating new dish: $name")
                val newDish = DishCreateDto(name, description)
                val createdDish = dishApi.createDish(newDish)
                Log.d(tag, "Successfully created dish with ID: ${createdDish.id}")
                Result.success(createdDish)
            } catch (e: Exception) {
                Log.e(tag, "Error creating dish: $name", e)
                Result.failure(e)
            }
        }

    /**
     * Update an existing dish with Android-specific error handling
     */
    suspend fun updateDish(
        id: Int,
        name: String?,
        description: String?,
    ): Result<DishDto> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Updating dish with ID: $id")
                val updateDish = DishUpdateDto(name, description)
                val updatedDish = dishApi.updateDish(id, updateDish)
                Log.d(tag, "Successfully updated dish: ${updatedDish.name}")
                Result.success(updatedDish)
            } catch (e: Exception) {
                Log.e(tag, "Error updating dish with ID: $id", e)
                Result.failure(e)
            }
        }

    /**
     * Archive a dish with Android-specific error handling
     */
    suspend fun archiveDish(id: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Archiving dish with ID: $id")
                val success = dishApi.archiveDish(id)
                Log.d(tag, "Archive operation result: $success")
                Result.success(success)
            } catch (e: Exception) {
                Log.e(tag, "Error archiving dish with ID: $id", e)
                Result.failure(e)
            }
        }

    /**
     * Update dish images by sending the full list of images (create/delete/set primary)
     */
    suspend fun updateDishImages(
        id: Int,
        imageFileIds: List<Int>?,
    ): Result<DishDto> =
        withContext(Dispatchers.IO) {
            try {
                val updated = dishApi.updateDish(id, DishUpdateDto(imageFileIds = imageFileIds))
                Result.success(updated)
            } catch (e: Exception) {
                Log.e(tag, "Error updating dish images for ID: $id", e)
                Result.failure(e)
            }
        }
}
