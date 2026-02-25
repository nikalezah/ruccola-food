package kz.ruccola.food.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.Strings
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.api.UserWithPasswordDto

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    authApi: AuthApi = AuthApi(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Users list loaded from the server (for testing/demo convenience)
    var users by remember { mutableStateOf<List<UserWithPasswordDto>>(emptyList()) }
    var loadingUsers by remember { mutableStateOf(false) }
    var usersError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadingUsers = true
        usersError = null
        try {
            users = authApi.getUsersWithPasswords()
        } catch (t: Throwable) {
            usersError = t.message
        } finally {
            loadingUsers = false
        }
    }

    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(Strings.login, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(Strings.email) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(Strings.password) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        // Users list (loaded from DB)
        if (loadingUsers) {
            Text(text = "Loading users…", style = MaterialTheme.typography.bodySmall)
        } else if (usersError != null) {
            Text(
                text = "Failed to load users: $usersError",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        } else if (users.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Users (tap to sign in):",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(users) { u ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Directly login with selected user's credentials
                                    scope.launch {
                                        error = null
                                        try {
                                            val resp = authApi.login(u.email.trim(), u.password)
                                            // Also reflect the UI fields in case the user returns
                                            email = u.email
                                            password = u.password
                                            onLoggedIn(resp)
                                        } catch (t: Throwable) {
                                            error = t.message ?: "Login failed"
                                        }
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text = u.email, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = u.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                scope.launch {
                    error = null
                    try {
                        val resp = authApi.login(email.trim(), password)
                        onLoggedIn(resp)
                    } catch (t: Throwable) {
                        error = t.message ?: Strings.loginFailed
                    }
                }
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(Strings.login)
        }
    }
}
