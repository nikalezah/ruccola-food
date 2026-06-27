package kz.ruccola.food.feature.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DayApi
import kz.ruccola.food.api.DayDto

class DayViewModel(
    private val api: DayApi = DayApi(),
) : ViewModel() {
    val uiState: StateFlow<DayUiState>
        field = MutableStateFlow(DayUiState())

    init {
        loadDays()
    }

    fun loadDays() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val days = api.getAllDays()
                uiState.update { it.copy(days = days.sortedByDescending { day -> day.date }, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Error", isLoading = false) }
            }
        }
    }

    fun triggerMidnight() {
        viewModelScope.launch {
            uiState.update { it.copy(isTriggeringMidnight = true, error = null) }
            try {
                api.triggerMidnight()
                loadDays()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message) }
            } finally {
                uiState.update { it.copy(isTriggeringMidnight = false) }
            }
        }
    }

    companion object {
        fun factory(api: DayApi = DayApi()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DayViewModel(api) }
            }
    }
}

data class DayUiState(
    val days: List<DayDto> = emptyList(),
    val isLoading: Boolean = false,
    val isTriggeringMidnight: Boolean = false,
    val error: String? = null,
)
