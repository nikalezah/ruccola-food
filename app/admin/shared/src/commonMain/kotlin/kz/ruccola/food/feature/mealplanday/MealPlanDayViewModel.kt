package kz.ruccola.food.feature.mealplanday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.MealPlanDayApi
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.model.Meal

class MealPlanDayViewModel(
    private val api: MealPlanDayApi = MealPlanDayApi(),
) : ViewModel() {
    val uiState: StateFlow<MealPlanDayUiState>
        field = MutableStateFlow(MealPlanDayUiState())

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = api.getAll()
                uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun save(
        id: Int?,
        dishIdToMeal: Map<Int, Meal>,
    ) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val saved = api.save(MealPlanDaySaveDto(id = id, dishIdToMeal = dishIdToMeal))
                uiState.update { current ->
                    val updated = current.items.toMutableList()
                    val idx = updated.indexOfFirst { it.id == saved.id }
                    if (idx >= 0) updated[idx] = saved else updated.add(saved)
                    current.copy(
                        items = updated.sortedBy { it.serial },
                        isSaving = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val success = api.delete(id)
                if (success) {
                    loadAll()
                } else {
                    uiState.update { it.copy(error = "Failed to delete") }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message) }
            } finally {
                uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun setCurrent(id: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.setCurrent(id)
                loadAll()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message) }
            } finally {
                uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun reorder(newOrderIds: List<Int>) {
        viewModelScope.launch {
            val current = uiState.value.items
            if (current.isEmpty() || newOrderIds.isEmpty()) return@launch
            val currentOrder = current.sortedBy { it.serial }.map { it.id }
            if (currentOrder == newOrderIds) return@launch

            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.reorder(newOrderIds)
                loadAll()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Failed to apply new order") }
            } finally {
                uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    companion object {
        fun factory(api: MealPlanDayApi = MealPlanDayApi()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { MealPlanDayViewModel(api) }
            }
    }
}

data class MealPlanDayUiState(
    val items: List<MealPlanDayDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)
