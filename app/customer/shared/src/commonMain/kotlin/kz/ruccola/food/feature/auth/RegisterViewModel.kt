package kz.ruccola.food.feature.auth

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
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.api.RegisterRequestDto

class RegisterViewModel(private val api: AuthApi = AuthApi()) : ViewModel() {
    val uiState: StateFlow<RegisterUiState>
        field = MutableStateFlow(RegisterUiState())

    fun updateEmail(email: String) = uiState.update { it.copy(email = email) }

    fun updatePassword(password: String) = uiState.update { it.copy(password = password) }

    fun updateConfirmPassword(confirm: String) = uiState.update { it.copy(confirmPassword = confirm) }

    fun updateFirstName(firstName: String) = uiState.update { it.copy(firstName = firstName) }

    fun updateLastName(lastName: String) = uiState.update { it.copy(lastName = lastName) }

    fun updateAddress(address: String) = uiState.update { it.copy(address = address) }

    fun register(onRegistered: (AuthResponseDto) -> Unit) {
        val state = uiState.value
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val resp =
                    api.register(
                        RegisterRequestDto(
                            email = state.email.trim(),
                            password = state.password,
                            confirmPassword = state.confirmPassword,
                            firstName = state.firstName.trim(),
                            lastName = state.lastName.trim(),
                            address = state.address.trim(),
                        )
                    )
                reset()
                onRegistered(resp)
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Registration failed") }
            } finally {
                uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reset() {
        uiState.value = RegisterUiState()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory { initializer { RegisterViewModel() } }
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
