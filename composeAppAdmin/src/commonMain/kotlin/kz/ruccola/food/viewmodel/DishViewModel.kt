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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishWithTranslationsDto
import kz.ruccola.food.paging.ApiPagingSource

class DishViewModel : ViewModel() {
    private val dishApi = DishApi()

    val uiState: StateFlow<DishUiState>
        field = MutableStateFlow(DishUiState())

    val dishes: Flow<PagingData<DishWithTranslationsDto>> =
        Pager(PagingConfig(pageSize = 20, initialLoadSize = 20)) {
            ApiPagingSource { page, size -> dishApi.getAllDishesWithTranslations(page, size) }
        }.flow.cachedIn(viewModelScope)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DishViewModel()
            }
        }
    }

    fun getDishById(id: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dish = dishApi.getDishByIdWithTranslations(id)
                uiState.update {
                    it.copy(
                        selectedDish = dish,
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                uiState.update {
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
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val success = dishApi.archiveDish(id)
                if (!success) {
                    uiState.update { it.copy(isLoading = false, error = "Error") }
                }
            } catch (e: Exception) {
                uiState.update {
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
    val dishes: List<DishWithTranslationsDto> = emptyList(),
    val selectedDish: DishWithTranslationsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
)
