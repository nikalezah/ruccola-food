package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.tab_chat
import food.composeappcustomer.generated.resources.tab_profile
import food.composeappcustomer.generated.resources.tab_schedule
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainScreen(
    token: String,
    language: String,
    themePreference: ThemePreference,
    onLogout: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isChatOpen by remember { mutableStateOf(false) }
    var hasUnreadChat by remember { mutableStateOf(false) }
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
                            showBadge = hasUnreadChat,
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
                        if (tab != 1) isChatOpen = false
                    },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    ScheduleScreen(token)
                }

                1 -> {
                    ChatListScreen(
                        token = token,
                        onChatOpenChanged = { isChatOpen = it },
                        onUnreadChanged = { hasUnreadChat = it },
                    )
                }

                2 -> {
                    val profileLanguage = when {
                        language.startsWith("ru") -> "ru"
                        language.startsWith("kk") -> "kk"
                        else -> "en"
                    }
                    ProfileScreen(
                        token = token,
                        onLoggedOut = onLogout,
                        onLanguageChanged = onLanguageChanged,
                        currentLanguage = profileLanguage,
                        themePreference = themePreference,
                        onThemePreferenceChanged = onThemePreferenceChanged,
                    )
                }
            }
        }
    }
}
