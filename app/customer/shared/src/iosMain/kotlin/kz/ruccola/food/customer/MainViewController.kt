package kz.ruccola.food.customer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kz.ruccola.food.App
import kz.ruccola.food.SessionViewModelStoreOwner
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.theme.ThemePreference
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

fun MainViewController() = ComposeUIViewController { CustomerApp() }

@Composable
fun CustomerApp() {
    var token by remember { mutableStateOf(IosPreferences.get("customer.token")) }
    var language by remember {
        mutableStateOf(IosPreferences.get("customer.language") ?: "ru")
    }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(IosPreferences.get("customer.theme")))
    }
    val isSystemDark = isSystemInDarkTheme()

    var sessionOwner by remember { mutableStateOf(SessionViewModelStoreOwner()) }

    fun resetSession() {
        sessionOwner.clear()
        sessionOwner = SessionViewModelStoreOwner()
    }

    LaunchedEffect(token) {
        TokenProvider.token = token
    }

    LaunchedEffect(language) {
        LanguageProvider.language = language
    }

    DisposableEffect(Unit) {
        TokenProvider.onUnauthorized = {
            token = null
            IosPreferences.remove("customer.token")
        }
        onDispose {
            TokenProvider.onUnauthorized = null
        }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides sessionOwner) {
        App(
            token = token,
            language = language,
            themePreference = themePreference,
            isSystemDark = isSystemDark,
            onLogin = { newToken ->
                resetSession()
                token = newToken
                IosPreferences.set("customer.token", newToken)
            },
            onRegister = { newToken ->
                resetSession()
                token = newToken
                IosPreferences.set("customer.token", newToken)
            },
            onLogout = {
                resetSession()
                token = null
                IosPreferences.remove("customer.token")
            },
            onLanguageChanged = { newLang ->
                language = newLang
                IosPreferences.set("customer.language", newLang)
            },
            onThemePreferenceChanged = { newPreference ->
                themePreference = newPreference
                IosPreferences.set("customer.theme", newPreference.storageValue())
            },
            onOpenWhatsApp = {
                val url = NSURL.URLWithString("https://wa.me/77059847909") ?: return@App
                UIApplication.sharedApplication.openURL(url)
            },
        )
    }
}
