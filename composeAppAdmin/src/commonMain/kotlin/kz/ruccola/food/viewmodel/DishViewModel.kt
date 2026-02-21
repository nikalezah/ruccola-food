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
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishUpdateDto

class DishViewModel : ViewModel() {
    private val dishApi = DishApi()

    private val _uiState = MutableStateFlow(DishUiState())
    val uiState: StateFlow<DishUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DishViewModel()
            }
        }
    }

    init {
        loadDishes()
    }

    fun loadDishes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dishes = dishApi.getAllDishes().filter { !it.archived }
                _uiState.update { it.copy(dishes = dishes, isLoading = false, error = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: Strings.error,
                    )
                }
            }
        }
    }

    fun getDishById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dish = dishApi.getDishById(id)
                _uiState.update {
                    it.copy(
                        selectedDish = dish,
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: Strings.error,
                    )
                }
            }
        }
    }

    fun createDish(
        name: String,
        description: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                dishApi.createDish(DishCreateDto(name = name, description = description))
                loadDishes()
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: Strings.error,
                    )
                }
            }
        }
    }

    fun updateDish(
        id: Int,
        name: String?,
        description: String?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                dishApi.updateDish(id, DishUpdateDto(name = name, description = description))
                loadDishes()
                _uiState.update { it.copy(showEditDialog = false, selectedDish = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: Strings.error,
                    )
                }
            }
        }
    }

    fun archiveDish(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val success = dishApi.archiveDish(id)
                if (success) {
                    loadDishes()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = Strings.error) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: Strings.error,
                    )
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(dish: DishDto) {
        _uiState.update { it.copy(showEditDialog = true, selectedDish = dish) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, selectedDish = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DishUiState(
    val dishes: List<DishDto> = emptyList(),
    val selectedDish: DishDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
)
