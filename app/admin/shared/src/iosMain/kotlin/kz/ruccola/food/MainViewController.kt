package kz.ruccola.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.theme.ThemePreference

fun MainViewController() = ComposeUIViewController { AdminApp() }

@Composable
fun AdminApp() {
    LanguageProvider.language = "ru"

    var role by remember { mutableStateOf(IosPreferences.get("admin.role")) }
    var token by remember { mutableStateOf(IosPreferences.get("admin.token")) }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(IosPreferences.get("admin.theme")))
    }

    val (sessionOwner, resetSession) = rememberAppSession()

    LaunchedEffect(token) {
        TokenProvider.token = token
    }

    DisposableEffect(Unit) {
        TokenProvider.onUnauthorized = {
            role = null
            token = null
            IosPreferences.remove("admin.role")
            IosPreferences.remove("admin.token")
        }
        onDispose {
            TokenProvider.onUnauthorized = null
        }
    }

    AppSessionProvider(sessionOwner) {
        App(
            role = role,
            token = token,
            themePreference = themePreference,
            onLoggedIn = { r, t ->
                resetSession()
                role = r
                token = t
                IosPreferences.set("admin.role", r)
                IosPreferences.set("admin.token", t)
            },
            onLoggedOut = {
                resetSession()
                role = null
                token = null
                IosPreferences.remove("admin.role")
                IosPreferences.remove("admin.token")
            },
            onThemePreferenceChange = { preference ->
                themePreference = preference
                IosPreferences.set("admin.theme", preference.storageValue())
            },
        )
    }
}
