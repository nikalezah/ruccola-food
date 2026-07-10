package kz.ruccola.food.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.ChatApi
import kz.ruccola.food.api.ChatListItemDto
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDetailsDto

class CustomersViewModel(private val api: CustomerApi = CustomerApi(), private val chatApi: ChatApi = ChatApi()) :
    ViewModel() {
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

    companion object {
        fun factory(api: CustomerApi = CustomerApi(), chatApi: ChatApi = ChatApi()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { CustomersViewModel(api, chatApi) }
            }
    }
}

data class CustomersUiState(
    val customers: List<CustomerDetailsDto> = emptyList(),
    val chats: Map<Int, ChatListItemDto> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
