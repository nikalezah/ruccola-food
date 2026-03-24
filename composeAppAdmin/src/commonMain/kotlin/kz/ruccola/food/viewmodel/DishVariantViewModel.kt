package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishVariantDto
import kz.ruccola.food.api.DishVariantSaveDto

data class DishVariantUiState(
    val description: String = "",
    val initialDescription: String = "",
    val initialCustomerIds: Set<Int> = emptySet(),
    val isBusy: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val allCustomers: List<CustomerDto>? = null,
    val selectedCustomerIds: Set<Int> = emptySet(),
    val savedVariant: DishVariantDto? = null,
) {
    val hasChanges: Boolean
        get() = description != initialDescription || selectedCustomerIds != initialCustomerIds
}

class DishVariantViewModel(
    private val dishId: Int,
    private val existingVariant: DishVariantDto? = null,
    private val initialCustomerIds: Set<Int>? = null,
) : ViewModel() {
    private val dishApi = DishApi()
    private val customerApi = CustomerApi()

    private val _uiState = MutableStateFlow(
        DishVariantUiState(
            description = existingVariant?.description ?: "",
            initialDescription = existingVariant?.description ?: "",
            selectedCustomerIds = initialCustomerIds ?: existingVariant?.customerIds?.toSet() ?: emptySet(),
            initialCustomerIds = initialCustomerIds ?: existingVariant?.customerIds?.toSet() ?: emptySet(),
        ),
    )
    val uiState: StateFlow<DishVariantUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            try {
                val customers = customerApi.getAll()
                _uiState.update { it.copy(allCustomers = customers) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun toggleCustomerSelection(customerId: Int) {
        _uiState.update { state ->
            val newSelected = if (state.selectedCustomerIds.contains(customerId)) {
                state.selectedCustomerIds - customerId
            } else {
                state.selectedCustomerIds + customerId
            }
            state.copy(selectedCustomerIds = newSelected)
        }
    }

    fun save() {
        val currentState = _uiState.value
        if (currentState.description.isBlank() || currentState.isBusy) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            try {
                val variant = if (existingVariant == null) {
                    dishApi.createVariant(dishId, DishVariantSaveDto(description = currentState.description.trim()))
                } else {
                    dishApi.updateVariant(
                        dishId,
                        existingVariant.id,
                        DishVariantSaveDto(description = currentState.description.trim()),
                    )
                }

                dishApi.setVariantCustomers(dishId, variant.id, currentState.selectedCustomerIds.toList())
                _uiState.update { it.copy(savedVariant = variant) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to save") }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }
}
