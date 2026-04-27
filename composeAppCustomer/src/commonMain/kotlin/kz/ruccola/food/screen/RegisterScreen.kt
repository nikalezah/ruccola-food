package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.address
import food.composeappcustomer.generated.resources.back_to_login
import food.composeappcustomer.generated.resources.confirm_password
import food.composeappcustomer.generated.resources.email
import food.composeappcustomer.generated.resources.first_name
import food.composeappcustomer.generated.resources.last_name
import food.composeappcustomer.generated.resources.password
import food.composeappcustomer.generated.resources.register
import food.composeappcustomer.generated.resources.registering
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.viewmodel.RegisterViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterScreen(
    onRegistered: (resp: AuthResponseDto) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel { RegisterViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

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

    Column(modifier = Modifier.padding(24.dp).statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.register), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(stringResource(Res.string.email)) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateFirstName(it) },
            label = { Text(stringResource(Res.string.first_name)) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateLastName(it) },
            label = { Text(stringResource(Res.string.last_name)) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { viewModel.updateAddress(it) },
            label = { Text(stringResource(Res.string.address)) },
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(stringResource(Res.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().handleTabAndEnter(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text(stringResource(Res.string.confirm_password)) },
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
            Text(if (uiState.isLoading) stringResource(Res.string.registering) else stringResource(Res.string.register))
        }
        Button(onClick = onBackToLogin, enabled = !uiState.isLoading) {
            Text(stringResource(Res.string.back_to_login))
        }
    }
}
