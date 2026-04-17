package kz.ruccola.food

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.theme.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.applyStoredLanguage(this)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val language by AppPreferences.languageTagFlow(context)
                .collectAsState(initial = AppLocaleManager.getCurrentLanguageTag(context))

            val themePreferenceString by AppPreferences.themePreferenceFlow(context)
                .collectAsState(initial = ThemePreference.SYSTEM.storageValue())
            val themePreference = remember(themePreferenceString) {
                ThemePreference.fromStorage(themePreferenceString)
            }

            val token by AppPreferences.tokenFlow(context)
                .collectAsState(initial = null)

            LaunchedEffect(language) {
                LanguageProvider.language = language ?: "ru"
            }

            var sessionOwner by remember { mutableStateOf(SessionViewModelStoreOwner()) }

            DisposableEffect(Unit) {
                TokenProvider.onUnauthorized = {
                    scope.launch {
                        AppPreferences.setToken(context, null)
                    }
                }
                onDispose {
                    TokenProvider.onUnauthorized = null
                }
            }

            CompositionLocalProvider(LocalViewModelStoreOwner provides sessionOwner) {
                App(
                    token = token,
                    language = language ?: AppLocaleManager.getCurrentLanguageTag(context),
                    themePreference = themePreference,
                    isSystemDark = isSystemInDarkTheme(),
                    onLogin = { newToken ->
                        sessionOwner.clear()
                        sessionOwner = SessionViewModelStoreOwner()
                        scope.launch { AppPreferences.setToken(context, newToken) }
                    },
                    onRegister = { newToken ->
                        sessionOwner.clear()
                        sessionOwner = SessionViewModelStoreOwner()
                        scope.launch { AppPreferences.setToken(context, newToken) }
                    },
                    onLogout = {
                        sessionOwner.clear()
                        sessionOwner = SessionViewModelStoreOwner()
                        scope.launch { AppPreferences.setToken(context, null) }
                    },
                    onLanguageChanged = { newLang ->
                        scope.launch {
                            AppLocaleManager.setLanguage(context, newLang)
                            (context as? android.app.Activity)?.recreate()
                        }
                    },
                    onThemePreferenceChanged = { newPreference ->
                        scope.launch {
                            AppPreferences.setThemePreference(context, newPreference.storageValue())
                        }
                    },
                    onOpenWhatsApp = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/77059847909".toUri())
                        context.startActivity(intent)
                    },
                )
            }
        }
    }
}
