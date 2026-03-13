package kz.ruccola.food

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kz.ruccola.food.api.Role
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
                            onLoggedIn(resp.user.role.name, resp.token)
                        },
                    )
                }

                role == Role.ADMIN.name -> {
                    MainScreen(
                        onLoggedOut = onLoggedOut,
                        themePreference = themePreference,
                        onThemePreferenceChange = onThemePreferenceChange,
                    )
                }
            }
        }
    }
}
