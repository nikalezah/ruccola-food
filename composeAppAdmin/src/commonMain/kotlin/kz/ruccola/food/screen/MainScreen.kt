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
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.tab_customers
import food.composeappadmin.generated.resources.tab_dishes
import food.composeappadmin.generated.resources.tab_meal_plan_days
import food.composeappadmin.generated.resources.tab_plans
import food.composeappadmin.generated.resources.tab_settings
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.BackHandler
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainScreen(
    token: String,
    onLoggedOut: () -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isChatOpen by remember { mutableStateOf(false) }
    var hasUnreadChats by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!isChatOpen) {
                LabeledNavigationBar(
                    tabs = listOf(
                        LabeledNavigationTab(
                            Icons.Filled.PriceChange,
                            Icons.Outlined.PriceChange,
                            stringResource(Res.string.tab_plans),
                        ),
                        LabeledNavigationTab(
                            Icons.Filled.DinnerDining,
                            Icons.Outlined.DinnerDining,
                            stringResource(Res.string.tab_dishes),
                        ),
                        LabeledNavigationTab(
                            Icons.Filled.CalendarMonth,
                            Icons.Outlined.CalendarMonth,
                            stringResource(Res.string.tab_meal_plan_days),
                        ),
                        LabeledNavigationTab(
                            Icons.Filled.Groups,
                            Icons.Outlined.Groups,
                            stringResource(Res.string.tab_customers),
                            showBadge = hasUnreadChats,
                        ),
                        LabeledNavigationTab(
                            Icons.Filled.Settings,
                            Icons.Outlined.Settings,
                            stringResource(Res.string.tab_settings),
                        ),
                    ),
                    selected = { selectedTab },
                    onSelect = { selectedTab = it },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            var isViewingHistory by remember(selectedTab) { mutableStateOf(false) }
            when (selectedTab) {
                0 -> {
                    PlanScreen()
                }

                1 -> {
                    DishScreen(token)
                }

                2 -> {
                    if (isViewingHistory) {
                        DayScreen { isViewingHistory = false }
                        BackHandler { isViewingHistory = false }
                    } else {
                        MealPlanDayScreen { isViewingHistory = true }
                    }
                }

                3 -> {
                    CustomerScreen(
                        token = token,
                        onChatOpenChanged = { isChatOpen = it },
                        onUnreadChanged = { hasUnreadChats = it },
                    )
                }

                4 -> {
                    SettingsScreen(
                        onLoggedOut = onLoggedOut,
                        themePreference = themePreference,
                        onThemePreferenceChange = onThemePreferenceChange,
                    )
                }

                else -> {
                    PlanScreen()
                }
            }
        }
    }
}
