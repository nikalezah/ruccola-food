package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.email
import food.composeappcustomer.generated.resources.go_to_register
import food.composeappcustomer.generated.resources.logging_in
import food.composeappcustomer.generated.resources.login
import food.composeappcustomer.generated.resources.login_failed
import food.composeappcustomer.generated.resources.password
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val loginFailedMsg = stringResource(Res.string.login_failed)

    fun tryLogin() {
        viewModel.login(onLoggedIn, loginFailedMsg)
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

    Column(modifier = Modifier.padding(24.dp).statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        if (uiState.error != null) {
            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { tryLogin() },
                enabled = !uiState.isLoading,
                modifier = Modifier.handleTabAndEnter(),
            ) {
                Text(
                    if (uiState.isLoading) {
                        stringResource(Res.string.logging_in)
                    } else {
                        stringResource(
                            Res.string.login,
                        )
                    },
                )
            }

            TextButton(onClick = onGoToRegister) {
                Text(stringResource(Res.string.go_to_register))
            }
        }
    }
}
