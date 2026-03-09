package kz.ruccola.food

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kz.ruccola.food.localization.LocalLocale
import kz.ruccola.food.screen.LoginScreen
import kz.ruccola.food.screen.MainScreen
import kz.ruccola.food.screen.RegisterScreen
import kz.ruccola.food.theme.GreenDarkColorScheme
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.theme.ThemePreference

@Composable
fun App(
    token: String?,
    language: String,
    themePreference: ThemePreference,
    isSystemDark: Boolean,
    onLogin: (String) -> Unit,
    onRegister: (String) -> Unit,
    onLogout: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
) {
    val colorScheme = when (themePreference) {
        ThemePreference.LIGHT -> GreenLightColorScheme
        ThemePreference.DARK -> GreenDarkColorScheme
        ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
    }

    CompositionLocalProvider(LocalLocale provides language) {
        MaterialTheme(colorScheme = colorScheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                var authScreen by remember { mutableStateOf("login") }

                if (token == null) {
                    when (authScreen) {
                        "register" -> RegisterScreen(
                            onRegistered = { resp -> onRegister(resp.token) },
                            onBackToLogin = { authScreen = "login" },
                        )

                        else -> LoginScreen(
                            onLoggedIn = { resp -> onLogin(resp.token) },
                            onGoToRegister = { authScreen = "register" },
                        )
                    }
                } else {
                    MainScreen(
                        token = token,
                        language = language,
                        themePreference = themePreference,
                        onLogout = onLogout,
                        onLanguageChanged = onLanguageChanged,
                        onThemePreferenceChanged = onThemePreferenceChanged,
                    )
                }
            }
        }
    }
}
