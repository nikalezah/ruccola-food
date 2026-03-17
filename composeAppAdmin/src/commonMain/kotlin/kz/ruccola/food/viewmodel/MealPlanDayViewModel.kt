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
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayApi
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.model.Meal

class MealPlanDayViewModel : ViewModel() {
    private val api = MealPlanDayApi()

    private val _uiState = MutableStateFlow(MealPlanDayUiState())
    val uiState: StateFlow<MealPlanDayUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MealPlanDayViewModel()
            }
        }
    }

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = api.getAll()
                _uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun save(
        id: Int?,
        dishIdToMeal: Map<Int, Meal>,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val saved = api.save(MealPlanDaySaveDto(id = id, dishIdToMeal = dishIdToMeal))
                _uiState.update { current ->
                    val updated = current.items.toMutableList()
                    val idx = updated.indexOfFirst { it.id == saved.id }
                    if (idx >= 0) updated[idx] = saved else updated.add(saved)
                    current.copy(
                        items = updated.sortedBy { it.serial },
                        selectedDishes = saved.dishes,
                        selectedDishesForId = saved.id,
                        isSaving = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val success = api.delete(id)
                if (success) {
                    loadAll()
                } else {
                    _uiState.update { it.copy(error = "Failed to delete") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun setCurrent(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.setCurrent(id)
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun reorder(newOrderIds: List<Int>) {
        viewModelScope.launch {
            val current = uiState.value.items
            if (current.isEmpty() || newOrderIds.isEmpty()) return@launch
            val currentOrder = current.sortedBy { it.serial }.map { it.id }
            if (currentOrder == newOrderIds) return@launch

            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.reorder(newOrderIds)
                loadAll()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to apply new order") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

data class MealPlanDayUiState(
    val items: List<MealPlanDayDto> = emptyList(),
    val selectedDishes: List<DishWithMealDto> = emptyList(),
    val selectedDishesForId: Int? = null,
    val isLoading: Boolean = false,
    val isLoadingDishes: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)
