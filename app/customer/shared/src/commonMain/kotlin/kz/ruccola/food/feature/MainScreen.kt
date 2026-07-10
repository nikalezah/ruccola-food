package kz.ruccola.food.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.tab_profile
import food.composeappcustomer.generated.resources.tab_schedule
import food.composeappcustomer.generated.resources.tab_subscription
import kz.ruccola.food.feature.profile.ProfileScreen
import kz.ruccola.food.feature.schedule.ScheduleScreen
import kz.ruccola.food.feature.subscription.SubscriptionScreen
import kz.ruccola.food.navigation.CustomerTab
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.AdaptiveNavigationScaffold
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.LabeledNavigationTab
import org.jetbrains.compose.resources.stringResource

private val bottomNavTabs: List<CustomerTab>
    get() =
        listOf(
            CustomerTab.Schedule,
            /*
            CustomerTab.Chat,
             */
            CustomerTab.Subscription,
            CustomerTab.Profile,
        )

@Composable
fun MainScreen(
    language: String,
    themePreference: ThemePreference,
    onLogout: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    onOpenWhatsApp: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(CustomerTab.Schedule) }
    var isChatOpen by remember { mutableStateOf(false) }
    var hasUnreadChat by remember { mutableStateOf(false) }
    val selectedTabIndex = bottomNavTabs.indexOf(selectedTab)
    val showBottomBar = !(selectedTabIndex == 1 && isChatOpen)

    AdaptiveNavigationScaffold(
        tabs =
            listOf(
                LabeledNavigationTab(
                    Icons.Filled.DinnerDining,
                    Icons.Outlined.DinnerDining,
                    stringResource(Res.string.tab_schedule),
                ),
                /*
                LabeledNavigationTab(
                    Icons.Filled.Chat,
                    Icons.Outlined.Chat,
                    stringResource(Res.string.tab_chat),
                    showBadge = hasUnreadChat,
                ),
                 */
                LabeledNavigationTab(
                    Icons.Filled.Settings,
                    Icons.Outlined.Settings,
                    stringResource(Res.string.tab_subscription),
                ),
                LabeledNavigationTab(
                    Icons.Filled.ManageAccounts,
                    Icons.Outlined.ManageAccounts,
                    stringResource(Res.string.tab_profile),
                ),
            ),
        selected = { selectedTabIndex },
        onSelect = { tab ->
            selectedTab = bottomNavTabs[tab]
            if (tab != 1) isChatOpen = false
        },
        showNavigation = showBottomBar,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                CustomerTab.Schedule -> {
                    ScheduleScreen()
                }

                /*
                CustomerTab.Chat -> {
                    ChatListScreen(
                        onChatOpenChanged = { isChatOpen = it },
                        onUnreadChanged = { hasUnreadChat = it },
                    )
                }
                 */

                CustomerTab.Subscription -> {
                    SubscriptionScreen()
                }

                CustomerTab.Profile -> {
                    val profileLanguage =
                        when {
                            language.startsWith("ru") -> "ru"
                            language.startsWith("kk") -> "kk"
                            else -> "en"
                        }
                    ProfileScreen(
                        onLoggedOut = onLogout,
                        onLanguageChanged = onLanguageChanged,
                        currentLanguage = profileLanguage,
                        themePreference = themePreference,
                        onThemePreferenceChanged = onThemePreferenceChanged,
                        onOpenWhatsApp = onOpenWhatsApp,
                    )
                }
            }
        }
    }
}
