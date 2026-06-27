package kz.ruccola.food.feature.dish

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
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishWithTranslationsDto
import kz.ruccola.food.paging.ApiPagingSource

class DishViewModel(
    private val dishApi: DishApi = DishApi(),
) : ViewModel() {
    val uiState: StateFlow<DishUiState>
        field = MutableStateFlow(DishUiState())

    val dishes: Flow<PagingData<DishDto>> =
        Pager(PagingConfig(pageSize = 20, initialLoadSize = 20)) {
            ApiPagingSource { page, size -> dishApi.getAllDishes(page, size) }
        }.flow.cachedIn(viewModelScope)

    fun getDishById(id: Int) {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dish = dishApi.getDishById(id)
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

    fun clearSelectedDish() {
        uiState.update { it.copy(selectedDish = null, isLoading = false, error = null) }
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

    companion object {
        fun factory(dishApi: DishApi = DishApi()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DishViewModel(dishApi) }
            }
    }
}

data class DishUiState(
    val selectedDish: DishWithTranslationsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
