package kz.ruccola.food

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kz.ruccola.food.api.Role
import kz.ruccola.food.feature.LoginScreen
import kz.ruccola.food.feature.MainScreen
import kz.ruccola.food.localization.LocalLocale
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.theme.resolveColorScheme

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
    CompositionLocalProvider(LocalLocale provides "ru") {
        MaterialTheme(colorScheme = resolveColorScheme(themePreference)) {
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
}
