package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.localization.AppPreferences
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

            App(
                token = token,
                language = language ?: AppLocaleManager.getCurrentLanguageTag(context),
                themePreference = themePreference,
                isSystemDark = isSystemInDarkTheme(),
                onLogin = { newToken ->
                    scope.launch { AppPreferences.setToken(context, newToken) }
                },
                onRegister = { newToken ->
                    scope.launch { AppPreferences.setToken(context, newToken) }
                },
                onLogout = {
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
            )
        }
    }
}
