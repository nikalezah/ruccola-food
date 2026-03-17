package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.ScheduledDayDto

class ScheduleViewModel : ViewModel() {
    private val api = CustomerApi()

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun loadSchedule(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isRefreshing) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val week = api.getSchedule()
                _uiState.update { it.copy(week = week, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }
}

data class ScheduleUiState(
    val week: List<ScheduledDayDto>? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
