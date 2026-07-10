package kz.ruccola.food.viewmodel

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
import kz.ruccola.food.api.Role

class LoginViewModel(private val authApi: AuthApi = AuthApi(), private val roleFilter: (Role) -> Boolean) :
    ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field = MutableStateFlow(LoginUiState())

    fun updateEmail(email: String) {
        uiState.update { it.copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        uiState.update { it.copy(password = password, error = null) }
    }

    fun login(onLoggedIn: (AuthResponseDto) -> Unit, loginFailedText: String) {
        val state = uiState.value
        if (state.isLoading) return
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.login(state.email.trim(), state.password)
                if (!roleFilter(response.user.role)) {
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

    companion object {
        fun factory(roleFilter: (Role) -> Boolean, authApi: AuthApi = AuthApi()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { LoginViewModel(authApi, roleFilter) }
            }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
