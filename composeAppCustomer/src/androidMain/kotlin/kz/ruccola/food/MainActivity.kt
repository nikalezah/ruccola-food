package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.tab_chat
import food.composeappcustomer.generated.resources.tab_profile
import food.composeappcustomer.generated.resources.tab_schedule
import kotlinx.coroutines.launch
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.localization.AppPreferences
import kz.ruccola.food.localization.LocalLocale
import kz.ruccola.food.screen.ChatListScreen
import kz.ruccola.food.screen.LoginScreen
import kz.ruccola.food.screen.ProfileScreen
import kz.ruccola.food.screen.RegisterScreen
import kz.ruccola.food.screen.ScheduleScreen
import kz.ruccola.food.theme.GreenDarkColorScheme
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab
import org.jetbrains.compose.resources.stringResource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.applyStoredLanguage(this)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val language by AppPreferences.languageTagFlow(context)
                .collectAsState(initial = AppLocaleManager.getCurrentLanguageTag(context))

            val themePreferenceString by AppPreferences.themePreferenceFlow(context)
                .collectAsState(initial = ThemePreference.SYSTEM.storageValue())
            val themePreference = remember(themePreferenceString) {
                ThemePreference.fromStorage(themePreferenceString)
            }

            val token by AppPreferences.tokenFlow(context)
                .collectAsState(initial = null)

            val isSystemDark = isSystemInDarkTheme()
            val colorScheme = when (themePreference) {
                ThemePreference.LIGHT -> GreenLightColorScheme
                ThemePreference.DARK -> GreenDarkColorScheme
                ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
            }

            CompositionLocalProvider(
                LocalLocale provides (language ?: AppLocaleManager.getCurrentLanguageTag(context)),
            ) {
                MaterialTheme(colorScheme = colorScheme) {
                    var showRegister by rememberSaveable { mutableStateOf(false) }

                    val scope = rememberCoroutineScope()
                    if (token == null) {
                        if (showRegister) {
                            RegisterScreen(
                                onRegistered = { resp ->
                                    scope.launch {
                                        AppPreferences.setToken(context, resp.token)
                                    }
                                },
                                onBackToLogin = { showRegister = false },
                            )
                        } else {
                            LoginScreen(
                                onLoggedIn = { resp ->
                                    scope.launch {
                                        AppPreferences.setToken(context, resp.token)
                                    }
                                },
                                onGoToRegister = { showRegister = true },
                            )
                        }
                    } else {
                        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                        var isChatOpen by rememberSaveable { mutableStateOf(false) }
                        val showBottomBar = !(selectedTab == 1 && isChatOpen)

                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) {
                                    LabeledNavigationBar(
                                        tabs = listOf(
                                            LabeledNavigationTab(
                                                Icons.Filled.DinnerDining,
                                                Icons.Outlined.DinnerDining,
                                                stringResource(Res.string.tab_schedule),
                                            ),
                                            LabeledNavigationTab(
                                                Icons.Filled.Chat,
                                                Icons.Outlined.Chat,
                                                stringResource(Res.string.tab_chat),
                                            ),
                                            LabeledNavigationTab(
                                                Icons.Filled.ManageAccounts,
                                                Icons.Outlined.ManageAccounts,
                                                stringResource(Res.string.tab_profile),
                                            ),
                                        ),
                                        selected = { selectedTab },
                                        onSelect = { tab ->
                                            selectedTab = tab
                                            if (tab != 1) {
                                                isChatOpen = false
                                            }
                                        },
                                    )
                                }
                            },
                        ) { padding ->
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                            ) {
                                when (selectedTab) {
                                    0 -> {
                                        ScheduleScreen(token = token!!)
                                    }

                                    1 -> {
                                        ChatListScreen(
                                            token = token!!,
                                            onChatOpenChanged = { isChatOpen = it },
                                        )
                                    }

                                    2 -> {
                                        val currentTag = language ?: AppLocaleManager.getCurrentLanguageTag(context)
                                        val selectedTag = when {
                                            currentTag.startsWith("ru") -> "ru"
                                            currentTag.startsWith("kk") -> "kk"
                                            else -> "en"
                                        }
                                        ProfileScreen(
                                            token = token!!,
                                            onLoggedOut = {
                                                scope.launch {
                                                    AppPreferences.setToken(context, null)
                                                }
                                            },
                                            currentLanguage = selectedTag,
                                            onLanguageChanged = { newLang ->
                                                scope.launch {
                                                    AppLocaleManager.setLanguage(context, newLang)
                                                    (context as? android.app.Activity)?.recreate()
                                                }
                                            },
                                            themePreference = themePreference,
                                            onThemePreferenceChanged = { newPreference ->
                                                scope.launch {
                                                    AppPreferences.setThemePreference(
                                                        context,
                                                        newPreference.storageValue(),
                                                    )
                                                }
                                            },
                                        )
                                    }

                                    else -> {
                                        ScheduleScreen(token = token!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
