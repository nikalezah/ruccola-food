package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.Strings
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.AuthResponseDto

class LoginViewModel : ViewModel() {
    private val authApi = AuthApi()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, loginError = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, loginError = null) }
    }

    fun login(onLoggedIn: (AuthResponseDto) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        performLogin(email, password, onLoggedIn)
    }

    private fun performLogin(
        email: String,
        password: String,
        onLoggedIn: (AuthResponseDto) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, loginError = null) }
            try {
                val resp = authApi.login(email, password)
                onLoggedIn(resp)
            } catch (t: Throwable) {
                _uiState.update { it.copy(loginError = t.message ?: Strings.loginFailed) }
            } finally {
                _uiState.update { it.copy(isLoggingIn = false) }
            }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false,
    val loginError: String? = null,
)
