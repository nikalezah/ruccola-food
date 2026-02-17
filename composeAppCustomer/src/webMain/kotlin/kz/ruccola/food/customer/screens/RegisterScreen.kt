package kz.ruccola.food.customer.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kz.ruccola.food.customer.LocalStrings

@Composable
fun RegisterScreen(
    onRegistered: (resp: AuthResponseDto) -> Unit,
    onBackToLogin: () -> Unit,
    authApi: AuthApi = AuthApi(),
) {
    val strings = LocalStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(strings.register, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(strings.email) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text(strings.firstName) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text(strings.lastName) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(strings.address) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text(strings.confirmPassword) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        loading = true
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
                            error = t.message ?: strings.registerFailed
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
            ) {
                Text(if (loading) strings.registering else strings.register)
            }

            TextButton(onClick = onBackToLogin) {
                Text(strings.backToLogin)
            }
        }
    }
}
