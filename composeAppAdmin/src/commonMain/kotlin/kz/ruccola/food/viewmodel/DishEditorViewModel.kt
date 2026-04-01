package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishUpdateDto

data class DishEditorUiState(
    val dish: DishDto? = null,
    val name: String = "",
    val description: String = "",
    val isBusy: Boolean = false,
    val error: String? = null,
)

class DishEditorViewModel(
    initialDish: DishDto?,
) : ViewModel() {
    private val dishApi = DishApi()

    val uiState: StateFlow<DishEditorUiState>
        field = MutableStateFlow(
            DishEditorUiState(
                dish = initialDish,
                name = initialDish?.name ?: "",
                description = initialDish?.description ?: "",
            ),
        )

    fun saveDish() {
        viewModelScope.launch {
            uiState.update { it.copy(isBusy = true, error = null) }
            try {
                val currentDish = uiState.value.dish
                val currentName = uiState.value.name.trim()
                val currentDescription = uiState.value.description.trim()

                if (currentDish == null) {
                    val created = dishApi.createDish(
                        DishCreateDto(name = currentName, description = currentDescription),
                    )
                    uiState.update { it.copy(dish = created, name = created.name, description = created.description) }
                } else {
                    val updated = dishApi.updateDish(
                        currentDish.id,
                        DishUpdateDto(name = currentName, description = currentDescription),
                    )
                    uiState.update { it.copy(dish = updated, name = updated.name, description = updated.description) }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Ошибка сохранения") }
            } finally {
                uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun onNameChange(newName: String) {
        uiState.update { it.copy(name = newName) }
    }

    fun onDescriptionChange(newDescription: String) {
        uiState.update { it.copy(description = newDescription) }
    }

    fun updateDishName(newName: String) {
        val id = uiState.value.dish?.id ?: return
        viewModelScope.launch {
            uiState.update { it.copy(isBusy = true, error = null) }
            try {
                val updated = dishApi.updateDish(id, DishUpdateDto(name = newName.trim()))
                uiState.update { it.copy(dish = updated, name = updated.name) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message) }
            } finally {
                uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun updateDishDescription(newDescription: String) {
        val id = uiState.value.dish?.id ?: return
        viewModelScope.launch {
            uiState.update { it.copy(isBusy = true, error = null) }
            try {
                val updated = dishApi.updateDish(id, DishUpdateDto(description = newDescription.trim()))
                uiState.update { it.copy(dish = updated, description = updated.description) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message) }
            } finally {
                uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun onDishUpdated(updated: DishDto) {
        uiState.update { it.copy(dish = updated) }
    }
}
