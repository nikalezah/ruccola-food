package kz.ruccola.food.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto

class CustomerRepository {
    private val api = CustomerApi()

    suspend fun getCustomers(token: String): Result<List<CustomerDto>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.getAll(token))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
