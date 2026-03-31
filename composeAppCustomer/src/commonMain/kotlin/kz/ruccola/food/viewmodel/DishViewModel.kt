package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto

class DishViewModel : ViewModel() {
    private val api = DishApi()

    val uiState: StateFlow<DishUiState>
        field = MutableStateFlow(DishUiState())

    fun loadDish(dishId: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dish = api.getDishById(dishId)
                uiState.update { it.copy(dish = dish) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class DishUiState(
    val dish: DishDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
