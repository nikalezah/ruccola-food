package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.localization.AppPreferences
import kz.ruccola.food.screen.ChatListScreen
import kz.ruccola.food.screen.LoginScreen
import kz.ruccola.food.screen.ProfileScreen
import kz.ruccola.food.screen.RegisterScreen
import kz.ruccola.food.screen.ScheduleScreen
import kz.ruccola.food.theme.GreenDarkColorScheme
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab

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

            val isSystemDark = isSystemInDarkTheme()
            val colorScheme = when (themePreference) {
                ThemePreference.LIGHT -> GreenLightColorScheme
                ThemePreference.DARK -> GreenDarkColorScheme
                ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
            }

            MaterialTheme(colorScheme = colorScheme) {
                var token by remember { mutableStateOf<String?>(null) }
                var showRegister by remember { mutableStateOf(false) }

                val strings = remember(language) {
                    when (language?.take(2)) {
                        "en" -> EnStrings
                        "kk" -> KkStrings
                        else -> RuStrings
                    }
                }

                CompositionLocalProvider(LocalStrings provides strings) {
                    val scope = rememberCoroutineScope()
                    if (token == null) {
                        if (showRegister) {
                            RegisterScreen(
                                onRegistered = { resp ->
                                    token = resp.token
                                },
                                onBackToLogin = { showRegister = false },
                            )
                        } else {
                            LoginScreen(
                                onLoggedIn = { resp ->
                                    token = resp.token
                                },
                                onGoToRegister = { showRegister = true },
                            )
                        }
                    } else {
                        var selectedTab by remember { mutableIntStateOf(0) }
                        var isChatOpen by remember { mutableStateOf(false) }
                        val showBottomBar = !(selectedTab == 1 && isChatOpen)

                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) {
                                    LabeledNavigationBar(
                                        tabs = listOf(
                                            LabeledNavigationTab(
                                                Icons.Filled.DinnerDining,
                                                Icons.Outlined.DinnerDining,
                                                stringResource(R.string.tab_dishes),
                                            ),
                                            LabeledNavigationTab(
                                                Icons.AutoMirrored.Filled.Chat,
                                                Icons.AutoMirrored.Outlined.Chat,
                                                stringResource(R.string.tab_chat),
                                            ),
                                            LabeledNavigationTab(
                                                Icons.Filled.ManageAccounts,
                                                Icons.Outlined.ManageAccounts,
                                                stringResource(R.string.tab_profile),
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
                                            onLoggedOut = { token = null },
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
