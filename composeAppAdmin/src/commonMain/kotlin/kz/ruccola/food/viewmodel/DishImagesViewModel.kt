package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.FileApi

data class DishImageItem(
    val fileId: Int,
    val url: String,
)

data class DishImagesUiState(
    val dish: DishDto,
    val workingList: List<DishImageItem> = emptyList(),
    val initialWorkingList: List<DishImageItem> = emptyList(),
    val isBusy: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
) {
    val hasChanges: Boolean
        get() = workingList.map { it.fileId } != initialWorkingList.map { it.fileId }
}

class DishImagesViewModel(
    initialDish: DishDto,
) : ViewModel() {
    private val dishApi = DishApi()
    private val fileApi = FileApi()

    private val initialWorkingList = initialDish.images.map { DishImageItem(it.fileId, it.url) }

    private val _uiState = MutableStateFlow(
        DishImagesUiState(
            dish = initialDish,
            workingList = initialWorkingList,
            initialWorkingList = initialWorkingList,
        ),
    )
    val uiState: StateFlow<DishImagesUiState> = _uiState.asStateFlow()

    private val initialIds = initialDish.images.map { it.fileId }.toSet()

    fun uploadImage(
        filename: String,
        mimeType: String,
        bytes: ByteArray,
    ) {
        if (_uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            try {
                val uploaded = fileApi.upload(filename, mimeType, bytes)
                _uiState.update { state ->
                    state.copy(
                        workingList = state.workingList + DishImageItem(fileId = uploaded.id, url = uploaded.url),
                        isBusy = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, error = e.message) }
            }
        }
    }

    fun removeImage(item: DishImageItem) {
        _uiState.update { state ->
            state.copy(workingList = state.workingList - item)
        }
    }

    fun moveUp(index: Int) {
        if (index <= 0) return
        _uiState.update { state ->
            val newList = state.workingList.toMutableList()
            val item = newList.removeAt(index)
            newList.add(index - 1, item)
            state.copy(workingList = newList)
        }
    }

    fun moveDown(index: Int) {
        if (index >= _uiState.value.workingList.size - 1) return
        _uiState.update { state ->
            val newList = state.workingList.toMutableList()
            val item = newList.removeAt(index)
            newList.add(index + 1, item)
            state.copy(workingList = newList)
        }
    }

    fun save() {
        val currentState = _uiState.value
        if (currentState.isBusy) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            val currentIds = currentState.workingList.map { it.fileId }
            try {
                val updated = dishApi.updateDish(currentState.dish.id, DishUpdateDto(imageFileIds = currentIds))
                val removedIds = initialIds - currentIds.toSet()
                for (id in removedIds) {
                    runCatching { fileApi.delete(id) }
                }
                _uiState.update { it.copy(dish = updated, isBusy = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isBusy = false, error = e.message ?: "Failed to save") }
            }
        }
    }
}
