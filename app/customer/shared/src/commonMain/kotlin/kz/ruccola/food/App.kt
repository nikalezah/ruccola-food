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
import kz.ruccola.food.feature.MainScreen
import kz.ruccola.food.feature.auth.LoginScreen
import kz.ruccola.food.feature.auth.RegisterScreen
import kz.ruccola.food.localization.LocalLocale
import kz.ruccola.food.navigation.AuthRoute
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.theme.resolveColorScheme

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
    onOpenWhatsApp: () -> Unit = {},
) {
    val colorScheme = resolveColorScheme(themePreference, isSystemDark)

    CompositionLocalProvider(LocalLocale provides language) {
        MaterialTheme(colorScheme = colorScheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                var authRoute by remember { mutableStateOf(AuthRoute.Login) }

                if (token == null) {
                    when (authRoute) {
                        AuthRoute.Register ->
                            RegisterScreen(
                                onRegistered = { resp -> onRegister(resp.token) },
                                onBackToLogin = { authRoute = AuthRoute.Login },
                            )

                        AuthRoute.Login ->
                            LoginScreen(
                                onLoggedIn = { resp -> onLogin(resp.token) },
                                onGoToRegister = { authRoute = AuthRoute.Register },
                            )
                    }
                } else {
                    MainScreen(
                        language = language,
                        themePreference = themePreference,
                        onLogout = onLogout,
                        onLanguageChanged = onLanguageChanged,
                        onThemePreferenceChanged = onThemePreferenceChanged,
                        onOpenWhatsApp = onOpenWhatsApp,
                    )
                }
            }
        }
    }
}
