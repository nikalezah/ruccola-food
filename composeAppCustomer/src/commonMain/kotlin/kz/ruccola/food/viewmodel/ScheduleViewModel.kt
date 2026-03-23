package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.ScheduledDayDto
import kz.ruccola.food.paging.ApiPagingSource

class ScheduleViewModel : ViewModel() {
    private val api = CustomerApi()

    val schedule: Flow<PagingData<ScheduledDayDto>> =
        Pager(PagingConfig(pageSize = 7, initialLoadSize = 7)) {
            ApiPagingSource { page, size -> api.getSchedule(page, size) }
        }.flow.cachedIn(viewModelScope)
}
