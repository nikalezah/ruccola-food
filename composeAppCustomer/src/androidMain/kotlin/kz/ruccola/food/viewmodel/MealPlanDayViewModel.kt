package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.model.Meal
import kz.ruccola.food.repository.MealPlanDayRepository

class MealPlanDayViewModel : ViewModel() {
    private val repo = MealPlanDayRepository()

    private val _uiState = MutableStateFlow(MealPlanDayUiState())
    val uiState: StateFlow<MealPlanDayUiState> = _uiState.asStateFlow()

    init {
        getAll()
    }

    fun getAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repo.getAll()
            res.onSuccess { list -> _uiState.update { it.copy(items = list, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun save(
        id: Int?,
        dishIdToMeal: Map<Int, Meal>,
    ) {
        viewModelScope.launch {
            val r = repo.save(id, dishIdToMeal)
            r.onSuccess { saved ->
                _uiState.update { current ->
                    val updated = current.items.toMutableList()
                    val idx = updated.indexOfFirst { it.id == saved.id }
                    if (idx >= 0) updated[idx] = saved else updated.add(saved)
                    current.copy(
                        items = updated.sortedBy { it.serial },
                        selectedDishes = saved.dishes,
                        selectedDishesForId = saved.id,
                        error = null,
                    )
                }
            }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            val r = repo.delete(id)
            r.onSuccess { getAll() }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun getDishes(id: Int) {
        viewModelScope.launch {
            // clear previous selection to avoid showing stale dishes for another day
            _uiState.update { it.copy(selectedDishes = emptyList(), selectedDishesForId = null) }
            val r = repo.getDishes(id)
            r.onSuccess { list -> _uiState.update { it.copy(selectedDishes = list, selectedDishesForId = id) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setCurrent(id: Int) {
        viewModelScope.launch {
            val r = repo.setCurrent(id)
            r.onSuccess { getAll() }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    /**
     * Persist a new order of MealPlanDays by renumbering serials from 1..n according to [newOrderIds].
     * Uses a two-phase update with temporary unique serials to avoid DB unique constraint conflicts.
     */
    fun reorder(newOrderIds: List<Int>) {
        viewModelScope.launch {
            val current = uiState.value.items
            if (current.isEmpty() || newOrderIds.isEmpty()) return@launch
            val currentOrder = current.sortedBy { it.serial }.map { it.id }
            if (currentOrder == newOrderIds) return@launch

            val r = repo.reorder(newOrderIds)
            r.onSuccess { getAll() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "Failed to apply new order") } }
        }
    }
}

data class MealPlanDayUiState(
    val items: List<MealPlanDayDto> = emptyList(),
    val selectedDishes: List<DishWithMealDto> = emptyList(),
    val selectedDishesForId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
