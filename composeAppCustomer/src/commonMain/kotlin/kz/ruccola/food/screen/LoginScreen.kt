package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.ruccola.food.LocalStrings
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

    fun tryLogin() {
        viewModel.login(onLoggedIn, strings.loginFailed)
    }

    fun Modifier.handleTabAndEnter(): Modifier =
        this.onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.Tab -> {
                    focusManager.moveFocus(if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
                    true
                }

                Key.Enter -> {
                    tryLogin()
                    true
                }

                else -> {
                    false
                }
            }
        }

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(strings.login, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(strings.email) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .handleTabAndEnter(),
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .handleTabAndEnter(),
        )

        if (uiState.error != null) {
            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { tryLogin() },
                enabled = !uiState.isLoading,
                modifier = Modifier.handleTabAndEnter(),
            ) {
                Text(if (uiState.isLoading) strings.loggingIn else strings.login)
            }

            TextButton(onClick = onGoToRegister) {
                Text(strings.goToRegister)
            }
        }
    }
}
