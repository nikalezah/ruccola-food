package kz.ruccola.food.customer

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import kz.ruccola.food.App
import kz.ruccola.food.api.TokenProvider
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

        LaunchedEffect(token) {
            TokenProvider.token = token
        }

        App(
            token = token,
            language = language,
            themePreference = themePreference,
            isSystemDark = isSystemDark,
            onLogin = { newToken ->
                token = newToken
                window.localStorage.setItem("customer.token", newToken)
            },
            onRegister = { newToken ->
                token = newToken
                window.localStorage.setItem("customer.token", newToken)
            },
            onLogout = {
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
        )
    }
}
