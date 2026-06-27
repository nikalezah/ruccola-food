package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kz.ruccola.food.api.LanguageProvider
import kz.ruccola.food.api.TokenProvider
import kz.ruccola.food.theme.ThemePreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.applyStoredLanguage(this)
        LanguageProvider.language = "ru"
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val themePrefString by AppPreferences.themeFlow(context).collectAsState(initial = null)
            val themePreference = ThemePreference.fromStorage(themePrefString)

            var role by remember { mutableStateOf<String?>(null) }
            var token by remember { mutableStateOf<String?>(null) }
            var loginLoading by remember { mutableStateOf(true) }
            val loginRolePref by AppPreferences.roleFlow(context).collectAsState(initial = null)
            val loginTokenPref by AppPreferences.tokenFlow(context).collectAsState(initial = null)

            LaunchedEffect(loginRolePref, loginTokenPref) {
                if (loginRolePref != null && loginTokenPref != null) {
                    role = loginRolePref
                    token = loginTokenPref
                }
                loginLoading = false
            }

            DisposableEffect(Unit) {
                TokenProvider.onUnauthorized = {
                    scope.launch {
                        role = null
                        token = null
                        AppPreferences.setRole(context, null)
                        AppPreferences.setToken(context, null)
                    }
                }
                onDispose {
                    TokenProvider.onUnauthorized = null
                }
            }

            val (sessionOwner, resetSession) = rememberAppSession()

            AppSessionProvider(sessionOwner) {
                App(
                    role = role,
                    token = token,
                    themePreference = themePreference,
                    onLoggedIn = { r, t ->
                        resetSession()
                        role = r
                        token = t
                        scope.launch {
                            AppPreferences.setRole(context, r)
                            AppPreferences.setToken(context, t)
                        }
                    },
                    onLoggedOut = {
                        resetSession()
                        role = null
                        token = null
                        scope.launch {
                            AppPreferences.setRole(context, null)
                            AppPreferences.setToken(context, null)
                        }
                    },
                    onThemePreferenceChange = { newPref ->
                        scope.launch {
                            AppPreferences.setTheme(context, newPref.storageValue())
                        }
                    },
                    isLoading = loginLoading,
                )
            }
        }
    }
}
