package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.paging.DishPagingSource

class DishViewModel : ViewModel() {
    private val dishApi = DishApi()

    private val _uiState = MutableStateFlow(DishUiState())
    val uiState: StateFlow<DishUiState> = _uiState.asStateFlow()

    val dishes: Flow<PagingData<DishDto>> =
        Pager(PagingConfig(pageSize = 20, initialLoadSize = 20)) { DishPagingSource(dishApi) }
            .flow.cachedIn(viewModelScope)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DishViewModel()
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
                        error = e.message ?: "Error",
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
                if (!success) {
                    _uiState.update { it.copy(isLoading = false, error = "Error") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error",
                    )
                }
            }
        }
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
