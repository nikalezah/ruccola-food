package kz.ruccola.food.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.CustomerUpdateDto

class ProfileViewModel(private val customerApi: CustomerApi = CustomerApi(), private val authApi: AuthApi = AuthApi()) :
    ViewModel() {
    val uiState: StateFlow<ProfileUiState>
        field = MutableStateFlow(ProfileUiState())

    fun loadProfile() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val customer = customerApi.get()
                uiState.update { it.copy(customer = customer) }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                authApi.logout()
            } catch (e: Exception) {
                // Ignore logout error
            }
            onLoggedOut()
        }
    }

    fun updateCustomer(firstName: String, lastName: String, address: String) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val updated =
                    customerApi.update(
                        CustomerUpdateDto(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            address = address.trim(),
                        )
                    )
                uiState.update { it.copy(customer = updated, isEditing = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(saveError = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun setEditing(editing: Boolean) {
        uiState.update { it.copy(isEditing = editing, saveError = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory { initializer { ProfileViewModel() } }
    }
}

data class ProfileUiState(
    val customer: CustomerDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
)
