package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.repository.DayRepository

class DayViewModel : ViewModel() {
    private val repository = DayRepository()

    private val _uiState = MutableStateFlow(DayUiState())
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    init {
        loadDays()
    }

    fun loadDays() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getAllDays()
            result.onSuccess { days ->
                _uiState.update { it.copy(days = days, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun triggerMidnight() {
        viewModelScope.launch {
            // Let server compute latest Day + 1 if date not provided
            val res = repository.triggerMidnight()
            res.onSuccess {
                loadDays()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
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
    val error: String? = null,
)
