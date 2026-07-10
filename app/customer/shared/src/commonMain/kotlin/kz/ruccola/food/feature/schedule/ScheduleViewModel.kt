package kz.ruccola.food.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.ScheduledDayDto
import kz.ruccola.food.paging.ApiPagingSource

class ScheduleViewModel(private val api: CustomerApi = CustomerApi()) : ViewModel() {
    val schedule: Flow<PagingData<ScheduledDayDto>> =
        Pager(PagingConfig(pageSize = 7, initialLoadSize = 7)) {
            ApiPagingSource { page, size -> api.getSchedule(page, size) }
        }
            .flow
            .cachedIn(viewModelScope)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory { initializer { ScheduleViewModel() } }
    }
}
