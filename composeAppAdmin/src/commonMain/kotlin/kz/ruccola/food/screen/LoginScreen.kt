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
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.email
import food.composeappadmin.generated.resources.logging_in
import food.composeappadmin.generated.resources.login
import food.composeappadmin.generated.resources.password
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    fun Modifier.handleTabAndEnter(): Modifier =
        this.onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.Tab -> {
                    focusManager.moveFocus(if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
                    true
                }

                Key.Enter -> {
                    viewModel.login(onLoggedIn)
                    true
                }

                else -> {
                    false
                }
            }
        }

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.login), style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(stringResource(Res.string.email)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .handleTabAndEnter(),
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(stringResource(Res.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .handleTabAndEnter(),
        )

        if (uiState.loginError != null) {
            Text(text = uiState.loginError!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { viewModel.login(onLoggedIn) },
            enabled = !uiState.isLoggingIn,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.handleTabAndEnter(),
        ) {
            Text(if (uiState.isLoggingIn) stringResource(Res.string.logging_in) else stringResource(Res.string.login))
        }
    }
}
