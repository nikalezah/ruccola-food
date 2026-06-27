package kz.ruccola.food.customer

import androidx.compose.foundation.isSystemInDarkTheme
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
import kz.ruccola.food.App
import kz.ruccola.food.AppSessionProvider
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.rememberAppSession
import kz.ruccola.food.theme.ThemePreference
import java.awt.Desktop
import java.net.URI

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Ruccola Food Customer",
            state = WindowState(width = 1200.dp, height = 800.dp),
        ) {
            CustomerApp()
        }
    }

@Composable
fun CustomerApp() {
    var token by remember { mutableStateOf(DesktopPreferences.get("customer.token")) }
    var language by remember {
        mutableStateOf(DesktopPreferences.get("customer.language") ?: "ru")
    }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(DesktopPreferences.get("customer.theme")))
    }
    val isSystemDark = isSystemInDarkTheme()

    val (sessionOwner, resetSession) = rememberAppSession()

    LaunchedEffect(token) {
        TokenProvider.token = token
    }

    LaunchedEffect(language) {
        LanguageProvider.language = language
    }

    DisposableEffect(Unit) {
        TokenProvider.onUnauthorized = {
            token = null
            DesktopPreferences.remove("customer.token")
        }
        onDispose {
            TokenProvider.onUnauthorized = null
        }
    }

    AppSessionProvider(sessionOwner) {
        App(
            token = token,
            language = language,
            themePreference = themePreference,
            isSystemDark = isSystemDark,
            onLogin = { newToken ->
                resetSession()
                token = newToken
                DesktopPreferences.set("customer.token", newToken)
            },
            onRegister = { newToken ->
                resetSession()
                token = newToken
                DesktopPreferences.set("customer.token", newToken)
            },
            onLogout = {
                resetSession()
                token = null
                DesktopPreferences.remove("customer.token")
            },
            onLanguageChanged = { newLang ->
                language = newLang
                DesktopPreferences.set("customer.language", newLang)
            },
            onThemePreferenceChanged = { newPreference ->
                themePreference = newPreference
                DesktopPreferences.set("customer.theme", newPreference.storageValue())
            },
            onOpenWhatsApp = {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI("https://wa.me/77059847909"))
                }
            },
        )
    }
}
