package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import kz.ruccola.food.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onRegistered: (resp: AuthResponseDto) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel { RegisterViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val strings = LocalStrings.current

    fun tryRegister() {
        viewModel.register(onRegistered)
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
                    tryRegister()
                    true
                }

                else -> {
                    false
                }
            }
        }

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(strings.register, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(strings.email) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateFirstName(it) },
            label = { Text(strings.firstName) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateLastName(it) },
            label = { Text(strings.lastName) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { viewModel.updateAddress(it) },
            label = { Text(strings.address) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text(strings.confirmPassword) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        if (uiState.error != null) {
            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { tryRegister() },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.handleTabAndEnter(),
            enabled = !uiState.isLoading,
        ) {
            Text(if (uiState.isLoading) strings.registering else strings.register)
        }
        Button(onClick = onBackToLogin, enabled = !uiState.isLoading) {
            Text(strings.backToLogin)
        }
    }
}
