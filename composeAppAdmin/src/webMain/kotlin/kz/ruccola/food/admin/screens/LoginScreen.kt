package kz.ruccola.food.admin.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.AuthResponseDto

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    authApi: AuthApi = AuthApi(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun tryLogin() {
        scope.launch {
            error = null
            try {
                val resp = authApi.login(email.trim(), password)
                onLoggedIn(resp)
            } catch (t: Throwable) {
                error = t.message ?: Strings.loginFailed
            }
        }
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
        Text(Strings.login, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(Strings.email) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .handleTabAndEnter(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(Strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .handleTabAndEnter(),
        )

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { tryLogin() },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier
                .handleTabAndEnter(),
        ) {
            Text(Strings.login)
        }
    }
}
