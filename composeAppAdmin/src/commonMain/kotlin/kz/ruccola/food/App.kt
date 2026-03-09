package kz.ruccola.food

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.ruccola.food.screen.LoginScreen
import kz.ruccola.food.screen.MainScreen
import kz.ruccola.food.theme.GreenDarkColorScheme
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.theme.ThemePreference

@Composable
fun App(
    role: String?,
    token: String?,
    themePreference: ThemePreference,
    onLoggedIn: (role: String, token: String) -> Unit,
    onLoggedOut: () -> Unit,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    isLoading: Boolean = false,
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themePreference) {
        ThemePreference.LIGHT -> GreenLightColorScheme
        ThemePreference.DARK -> GreenDarkColorScheme
        ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                role == null || token == null -> {
                    LoginScreen(
                        onLoggedIn = { resp ->
                            onLoggedIn(resp.user.role, resp.token)
                        },
                    )
                }

                role == "ADMIN" -> {
                    MainScreen(
                        token = token,
                        onLoggedOut = onLoggedOut,
                        themePreference = themePreference,
                        onThemePreferenceChange = onThemePreferenceChange,
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    ) {
                        Text(
                            text = "Admin access only. Please log in with an admin account.",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Button(
                            onClick = onLoggedOut,
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
                            Text("Log out")
                        }
                    }
                }
            }
        }
    }
}
