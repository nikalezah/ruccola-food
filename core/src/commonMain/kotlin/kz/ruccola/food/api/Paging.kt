package kz.ruccola.food.api

import kotlinx.serialization.Serializable

@Serializable
data class PagingResponse<T>(
    val items: List<T>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
) {
    val totalPages: Int = if (size > 0) (totalCount / size).toInt() + (if (totalCount % size > 0L) 1 else 0) else 0
    val hasNext: Boolean = page < totalPages - 1
}
