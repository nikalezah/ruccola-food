package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.repository.DishRepository

/**
 * ViewModel for dish management on Android
 */
class DishViewModel : ViewModel() {
    private val repository = DishRepository()

    // UI state
    private val _uiState = MutableStateFlow(DishUiState())
    val uiState: StateFlow<DishUiState> = _uiState.asStateFlow()

    init {
        loadDishes()
    }

    /**
     * Load all dishes
     */
    fun loadDishes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getAllDishes().fold(
                onSuccess = { dishes ->
                    _uiState.update {
                        it.copy(
                            dishes = dishes,
                            isLoading = false,
                            error = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred",
                        )
                    }
                },
            )
        }
    }

    /**
     * Get a dish by ID
     */
    fun getDishById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getDishById(id).fold(
                onSuccess = { dish ->
                    _uiState.update {
                        it.copy(
                            selectedDish = dish,
                            isLoading = false,
                            error = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred",
                        )
                    }
                },
            )
        }
    }

    /**
     * Create a new dish
     */
    fun createDish(
        name: String,
        description: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.createDish(name, description).fold(
                onSuccess = { _ ->
                    // Reload dishes after successful creation
                    loadDishes()
                    _uiState.update {
                        it.copy(
                            showAddDialog = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred",
                        )
                    }
                },
            )
        }
    }

    /**
     * Update an existing dish
     */
    fun updateDish(
        id: Int,
        name: String?,
        description: String?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.updateDish(id, name, description).fold(
                onSuccess = { _ ->
                    // Reload dishes after successful update
                    loadDishes()
                    _uiState.update {
                        it.copy(
                            showEditDialog = false,
                            selectedDish = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred",
                        )
                    }
                },
            )
        }
    }

    /**
     * Archive a dish
     */
    fun archiveDish(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.archiveDish(id).fold(
                onSuccess = { success ->
                    if (success) {
                        // Reload dishes after successful archive
                        loadDishes()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to archive dish",
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred",
                        )
                    }
                },
            )
        }
    }

    // Dialog control functions
    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(dish: DishDto) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                selectedDish = dish,
            )
        }
    }

    fun hideEditDialog() {
        _uiState.update {
            it.copy(
                showEditDialog = false,
                selectedDish = null,
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for dish management
 */
data class DishUiState(
    val dishes: List<DishDto> = emptyList(),
    val selectedDish: DishDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
)
