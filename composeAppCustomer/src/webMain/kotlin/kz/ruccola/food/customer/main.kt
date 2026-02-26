package kz.ruccola.food.customer

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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import kz.ruccola.food.EnStrings
import kz.ruccola.food.KkStrings
import kz.ruccola.food.LocalStrings
import kz.ruccola.food.RuStrings
import kz.ruccola.food.customer.screens.ChatListScreen
import kz.ruccola.food.customer.screens.LoginScreen
import kz.ruccola.food.customer.screens.ProfileScreen
import kz.ruccola.food.screen.RegisterScreen
import kz.ruccola.food.screen.ScheduleScreen
import kz.ruccola.food.theme.GreenDarkColorScheme
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "root") {
        App()
    }
}

@Composable
fun App() {
    var token by remember { mutableStateOf(window.localStorage.getItem("customer.token")) }
    var screen by remember { mutableStateOf(if (token != null) "main" else "login") }
    var language by remember { mutableStateOf(window.localStorage.getItem("customer.language") ?: "ru") }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(window.localStorage.getItem("customer.theme")))
    }
    val isSystemDark = remember { window.matchMedia("(prefers-color-scheme: dark)").matches }
    val colorScheme = when (themePreference) {
        ThemePreference.LIGHT -> GreenLightColorScheme
        ThemePreference.DARK -> GreenDarkColorScheme
        ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
    }

    val strings = when (language) {
        "en" -> EnStrings
        "kk" -> KkStrings
        else -> RuStrings
    }

    CompositionLocalProvider(LocalStrings provides strings) {
        MaterialTheme(colorScheme = colorScheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                when {
                    token == null -> {
                        when (screen) {
                            "register" -> RegisterScreen(
                                onRegistered = { resp ->
                                    token = resp.token
                                    window.localStorage.setItem("customer.token", resp.token)
                                    screen = "main"
                                },
                                onBackToLogin = { screen = "login" },
                            )

                            else -> LoginScreen(
                                onLoggedIn = { resp ->
                                    token = resp.token
                                    window.localStorage.setItem("customer.token", resp.token)
                                    screen = "main"
                                },
                                onGoToRegister = { screen = "register" },
                            )
                        }
                    }

                    else -> {
                        MainScreen(
                            token!!,
                            onLoggedOut = {
                                token = null
                                window.localStorage.removeItem("customer.token")
                                screen = "login"
                            },
                            onLanguageChanged = {
                                language = it
                                window.localStorage.setItem("customer.language", it)
                            },
                            themePreference = themePreference,
                            onThemePreferenceChanged = { preference ->
                                themePreference = preference
                                window.localStorage.setItem("customer.theme", preference.storageValue())
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    token: String,
    onLoggedOut: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isChatOpen by remember { mutableStateOf(false) }
    var hasUnreadChat by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    val showBottomBar = !(selectedTab == 1 && isChatOpen)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LabeledNavigationBar(
                    tabs = listOf(
                        LabeledNavigationTab(
                            Icons.Filled.DinnerDining,
                            Icons.Outlined.DinnerDining,
                            strings.tabSchedule,
                        ),
                        LabeledNavigationTab(
                            Icons.AutoMirrored.Filled.Chat,
                            Icons.AutoMirrored.Outlined.Chat,
                            strings.tabChat,
                            showBadge = hasUnreadChat,
                        ),
                        LabeledNavigationTab(
                            Icons.Filled.ManageAccounts,
                            Icons.Outlined.ManageAccounts,
                            strings.tabProfile,
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
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> ScheduleScreen(token)

                1 -> ChatListScreen(
                    token = token,
                    onChatOpenChanged = { isChatOpen = it },
                    onUnreadChanged = { hasUnreadChat = it },
                )

                2 -> ProfileScreen(
                    token,
                    onLoggedOut = onLoggedOut,
                    onLanguageChanged = onLanguageChanged,
                    themePreference = themePreference,
                    onThemePreferenceChanged = onThemePreferenceChanged,
                )
            }
        }
    }
}
