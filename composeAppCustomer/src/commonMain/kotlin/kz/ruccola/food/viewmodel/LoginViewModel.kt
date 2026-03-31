package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.AuthResponseDto

class LoginViewModel : ViewModel() {
    private val authApi = AuthApi()

    val uiState: StateFlow<LoginUiState>
        field = MutableStateFlow(LoginUiState())

    fun updateEmail(email: String) {
        uiState.update { it.copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        uiState.update { it.copy(password = password, error = null) }
    }

    fun login(
        onLoggedIn: (AuthResponseDto) -> Unit,
        loginFailedText: String,
    ) {
        val state = uiState.value
        if (state.isLoading) return
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.login(state.email.trim(), state.password)
                if (!response.user.role.isCustomer) {
                    throw IllegalStateException("Login failed")
                }
                reset()
                onLoggedIn(response)
            } catch (t: Throwable) {
                uiState.update { it.copy(error = t.message ?: loginFailedText) }
            } finally {
                uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reset() {
        uiState.value = LoginUiState()
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
