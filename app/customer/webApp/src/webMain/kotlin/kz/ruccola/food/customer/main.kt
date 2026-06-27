package kz.ruccola.food.customer

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import kz.ruccola.food.App
import kz.ruccola.food.AppSessionProvider
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.rememberAppSession
import kz.ruccola.food.theme.ThemePreference

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "root") {
        var token by remember { mutableStateOf(window.localStorage.getItem("customer.token")) }
        var language by remember {
            mutableStateOf(window.localStorage.getItem("customer.language") ?: "ru")
        }
        var themePreference by remember {
            mutableStateOf(ThemePreference.fromStorage(window.localStorage.getItem("customer.theme")))
        }
        val isSystemDark = remember { window.matchMedia("(prefers-color-scheme: dark)").matches }

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
                window.localStorage.removeItem("customer.token")
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
                    window.localStorage.setItem("customer.token", newToken)
                },
                onRegister = { newToken ->
                    resetSession()
                    token = newToken
                    window.localStorage.setItem("customer.token", newToken)
                },
                onLogout = {
                    resetSession()
                    token = null
                    window.localStorage.removeItem("customer.token")
                },
                onLanguageChanged = { newLang ->
                    language = newLang
                    window.localStorage.setItem("customer.language", newLang)
                },
                onThemePreferenceChanged = { newPreference ->
                    themePreference = newPreference
                    window.localStorage.setItem("customer.theme", newPreference.storageValue())
                },
                onOpenWhatsApp = {
                    window.open("https://wa.me/77059847909", "_blank")
                },
            )
        }
    }
}
