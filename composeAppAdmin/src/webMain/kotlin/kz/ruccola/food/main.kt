package kz.ruccola.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import kz.ruccola.food.theme.ThemePreference

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "root") {
        AdminApp()
    }
}

@Composable
fun AdminApp() {
    var role by remember { mutableStateOf(window.localStorage.getItem("admin.role")) }
    var token by remember { mutableStateOf(window.localStorage.getItem("admin.token")) }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(window.localStorage.getItem("admin.theme")))
    }

    App(
        role = role,
        token = token,
        themePreference = themePreference,
        onLoggedIn = { r, t ->
            role = r
            token = t
            window.localStorage.setItem("admin.role", r)
            window.localStorage.setItem("admin.token", t)
        },
        onLoggedOut = {
            role = null
            token = null
            window.localStorage.removeItem("admin.role")
            window.localStorage.removeItem("admin.token")
        },
        onThemePreferenceChange = { preference ->
            themePreference = preference
            window.localStorage.setItem("admin.theme", preference.storageValue())
        },
    )
}
