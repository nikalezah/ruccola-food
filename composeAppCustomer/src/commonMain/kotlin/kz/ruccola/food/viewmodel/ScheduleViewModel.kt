package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.ScheduledDayDto
import kz.ruccola.food.paging.ApiPagingSource

class ScheduleViewModel : ViewModel() {
    private val api = CustomerApi()

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    val schedule: Flow<PagingData<ScheduledDayDto>> =
        Pager(PagingConfig(pageSize = 7, initialLoadSize = 7)) {
            ApiPagingSource { page, size -> api.getSchedule(page, size) }
        }.flow.cachedIn(viewModelScope)

    fun loadSchedule(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val response = api.getSchedule(0, 7)
                val scheduledDays = response.items
                _uiState.update { it.copy(scheduledDays = scheduledDays, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }
}

data class ScheduleUiState(
    val scheduledDays: List<ScheduledDayDto>? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
