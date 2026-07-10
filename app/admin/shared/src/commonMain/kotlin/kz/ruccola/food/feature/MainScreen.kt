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
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.tab_customers
import food.composeappadmin.generated.resources.tab_dishes
import food.composeappadmin.generated.resources.tab_meal_plan_days
import food.composeappadmin.generated.resources.tab_plans
import food.composeappadmin.generated.resources.tab_settings
import kz.ruccola.food.feature.customer.CustomerScreen
import kz.ruccola.food.feature.day.DayScreen
import kz.ruccola.food.feature.dish.DishScreen
import kz.ruccola.food.feature.mealplanday.MealPlanDayScreen
import kz.ruccola.food.feature.plan.PlanScreen
import kz.ruccola.food.navigation.AdminTab
import kz.ruccola.food.navigation.MealPlanDaysRoute
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.AdaptiveNavigationScaffold
import kz.ruccola.food.ui.BackHandler
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.LabeledNavigationTab
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainScreen(
    onLoggedOut: () -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(AdminTab.Plans) }
    var isChatOpen by remember { mutableStateOf(false) }
    var hasUnreadChats by remember { mutableStateOf(false) }

    AdaptiveNavigationScaffold(
        tabs =
            listOf(
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
        selected = { AdminTab.entries.indexOf(selectedTab) },
        onSelect = { selectedTab = AdminTab.entries[it] },
        showNavigation = !isChatOpen,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            var mealPlanDaysRoute by remember(selectedTab) { mutableStateOf(MealPlanDaysRoute.List) }
            when (selectedTab) {
                AdminTab.Plans -> {
                    PlanScreen()
                }

                AdminTab.Dishes -> {
                    DishScreen()
                }

                AdminTab.MealPlanDays -> {
                    when (mealPlanDaysRoute) {
                        MealPlanDaysRoute.History -> {
                            DayScreen { mealPlanDaysRoute = MealPlanDaysRoute.List }
                            BackHandler { mealPlanDaysRoute = MealPlanDaysRoute.List }
                        }

                        MealPlanDaysRoute.List -> {
                            MealPlanDayScreen { mealPlanDaysRoute = MealPlanDaysRoute.History }
                        }
                    }
                }

                AdminTab.Customers -> {
                    CustomerScreen(onChatOpenChanged = { isChatOpen = it }, onUnreadChanged = { hasUnreadChats = it })
                }

                AdminTab.Settings -> {
                    SettingsScreen(
                        onLoggedOut = onLoggedOut,
                        themePreference = themePreference,
                        onThemePreferenceChange = onThemePreferenceChange,
                    )
                }
            }
        }
    }
}
