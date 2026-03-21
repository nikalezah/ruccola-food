package kz.ruccola.food.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto

class DishPagingSource(
    private val dishApi: DishApi,
) : PagingSource<Int, DishDto>() {
    override fun getRefreshKey(state: PagingState<Int, DishDto>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DishDto> {
        val page = params.key ?: 0
        return try {
            val response = dishApi.getAllDishes(page = page, size = params.loadSize)
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.hasNext) page + 1 else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
