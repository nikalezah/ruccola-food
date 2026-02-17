package kz.ruccola.food.screens

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.api.RegisterRequestDto

@Composable
fun RegisterScreen(
    onRegistered: (resp: AuthResponseDto) -> Unit,
    onBackToLogin: () -> Unit,
    authApi: AuthApi = AuthApi(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Register")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First name") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last name") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = {
            scope.launch {
                error = null
                try {
                    val resp = authApi.register(
                        RegisterRequestDto(
                            email = email.trim(),
                            password = password,
                            confirmPassword = confirm,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            address = address.trim(),
                        ),
                    )
                    onRegistered(resp)
                } catch (t: Throwable) {
                    error = t.message ?: "Registration failed"
                }
            }
        }, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Create account")
        }
        Button(onClick = onBackToLogin) { Text("Back to login") }
    }
}
