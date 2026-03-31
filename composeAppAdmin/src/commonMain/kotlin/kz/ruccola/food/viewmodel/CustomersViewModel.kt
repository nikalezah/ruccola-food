package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatListItemDto
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto

class CustomersViewModel : ViewModel() {
    private val api = CustomerApi()
    private val chatApi = ChatApi()

    val uiState: StateFlow<CustomersUiState>
        field = MutableStateFlow(CustomersUiState())

    fun loadCustomers() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val loadedCustomers = api.getAll()
                val chatItems = chatApi.getChats()
                uiState.update {
                    it.copy(
                        customers = loadedCustomers,
                        chats = chatItems.associateBy { chat -> chat.customerId },
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Error", isLoading = false) }
            }
        }
    }
}

data class CustomersUiState(
    val customers: List<CustomerDto> = emptyList(),
    val chats: Map<Int, ChatListItemDto> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
