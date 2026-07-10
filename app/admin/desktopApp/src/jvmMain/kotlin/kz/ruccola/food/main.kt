package kz.ruccola.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.theme.ThemePreference

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ruccola Food Admin",
        state = WindowState(width = 1200.dp, height = 800.dp),
    ) {
        AdminApp()
    }
}

@Composable
fun AdminApp() {
    LanguageProvider.language = "ru"

    var role by remember { mutableStateOf(DesktopPreferences.get("admin.role")) }
    var token by remember { mutableStateOf(DesktopPreferences.get("admin.token")) }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(DesktopPreferences.get("admin.theme")))
    }

    val (sessionOwner, resetSession) = rememberAppSession()

    LaunchedEffect(token) { TokenProvider.token = token }

    DisposableEffect(Unit) {
        TokenProvider.onUnauthorized = {
            role = null
            token = null
            DesktopPreferences.remove("admin.role")
            DesktopPreferences.remove("admin.token")
        }
        onDispose { TokenProvider.onUnauthorized = null }
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
                DesktopPreferences.set("admin.role", r)
                DesktopPreferences.set("admin.token", t)
            },
            onLoggedOut = {
                resetSession()
                role = null
                token = null
                DesktopPreferences.remove("admin.role")
                DesktopPreferences.remove("admin.token")
            },
            onThemePreferenceChange = { preference ->
                themePreference = preference
                DesktopPreferences.set("admin.theme", preference.storageValue())
            },
        )
    }
}
