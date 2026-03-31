package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.PlanApi
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays

class PlanViewModel : ViewModel() {
    private val api = PlanApi()

    val uiState: StateFlow<PlanUiState>
        field = MutableStateFlow(PlanUiState())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlanViewModel()
            }
        }
    }

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val plans = api.getAll()
                uiState.update { it.copy(items = plans, isLoading = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun create(
        calories: PlanCalories,
        periodDays: PlanDays,
        pricePerDay: Int,
        allowVariantChoice: Boolean,
    ) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.create(PlanCreateDto(calories, periodDays, pricePerDay, allowVariantChoice))
                uiState.update { it.copy(isSaving = false) }
                loadAll()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Unknown error", isSaving = false) }
            }
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
            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.update(id, PlanUpdateDto(calories, periodDays, pricePerDay, allowVariantChoice))
                uiState.update { it.copy(isSaving = false) }
                loadAll()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Unknown error", isSaving = false) }
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, error = null) }
            try {
                api.delete(id)
                uiState.update { it.copy(isSaving = false) }
                loadAll()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Unknown error", isSaving = false) }
            }
        }
    }

    fun clearError() {
        uiState.update { it.copy(error = null) }
    }
}

data class PlanUiState(
    val items: List<PlanDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)
