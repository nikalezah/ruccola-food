package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.repository.PlanRepository

class PlanViewModel : ViewModel() {
    private val repo = PlanRepository()

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repo.getAll()
            res.onSuccess { list -> _uiState.update { it.copy(items = list, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun create(
        calories: PlanCalories,
        periodDays: PlanDays,
        pricePerDay: Int,
        allowVariantChoice: Boolean,
    ) {
        viewModelScope.launch {
            val r = repo.create(calories, periodDays, pricePerDay, allowVariantChoice)
            r.onSuccess { loadAll() }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun update(
        id: Int,
        calories: PlanCalories?,
        periodDays: PlanDays?,
        pricePerDay: Int?,
        allowVariantChoice: Boolean?,
    ) {
        viewModelScope.launch {
            val r = repo.update(id, calories, periodDays, pricePerDay, allowVariantChoice)
            r.onSuccess { loadAll() }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            val r = repo.delete(id)
            r.onSuccess { loadAll() }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class PlanUiState(
    val items: List<PlanDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
