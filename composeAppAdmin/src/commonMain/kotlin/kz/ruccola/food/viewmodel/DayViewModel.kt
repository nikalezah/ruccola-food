package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DayApi
import kz.ruccola.food.api.DayDto

class DayViewModel : ViewModel() {
    private val api = DayApi()

    private val _uiState = MutableStateFlow(DayUiState())
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DayViewModel()
            }
        }
    }

    init {
        loadDays()
    }

    fun loadDays() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val days = api.getAllDays()
                _uiState.update { it.copy(days = days.sortedByDescending { day -> day.date }, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Error", isLoading = false) }
            }
        }
    }

    fun triggerMidnight() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTriggeringMidnight = true, error = null) }
            try {
                api.triggerMidnight()
                loadDays()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isTriggeringMidnight = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DayUiState(
    val days: List<DayDto> = emptyList(),
    val isLoading: Boolean = false,
    val isTriggeringMidnight: Boolean = false,
    val error: String? = null,
)
