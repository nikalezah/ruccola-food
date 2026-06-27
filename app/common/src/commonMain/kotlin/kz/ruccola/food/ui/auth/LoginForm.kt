package kz.ruccola.food.ui.auth

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kz.ruccola.food.viewmodel.LoginUiState

@Composable
fun LoginForm(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    title: String,
    emailLabel: String,
    passwordLabel: String,
    loginLabel: String,
    loggingInLabel: String,
    modifier: Modifier = Modifier,
    extraActions: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(24.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text(emailLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().tabAndEnterOnSubmit(onSubmit),
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text(passwordLabel) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().tabAndEnterOnSubmit(onSubmit),
        )

        if (state.error != null) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.tabAndEnterOnSubmit(onSubmit),
            ) {
                Text(if (state.isLoading) loggingInLabel else loginLabel)
            }
            extraActions()
        }
    }
}
