package kz.ruccola.food.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PriceChange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.log_out
import food.composeappadmin.generated.resources.tab_customers
import food.composeappadmin.generated.resources.tab_dishes
import food.composeappadmin.generated.resources.tab_meal_plan_days
import food.composeappadmin.generated.resources.tab_plans
import food.composeappadmin.generated.resources.tab_settings
import kotlinx.browser.window
import kz.ruccola.food.screens.CustomerScreen
import kz.ruccola.food.screens.DayScreen
import kz.ruccola.food.screens.DishScreen
import kz.ruccola.food.screens.LoginScreen
import kz.ruccola.food.screens.MealPlanDayScreen
import kz.ruccola.food.screens.PlanScreen
import kz.ruccola.food.screens.SettingsScreen
import kz.ruccola.food.theme.GreenDarkColorScheme
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.BackHandler
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "root") {
        AdminApp()
    }
}

@Composable
fun AdminApp() {
    var route by remember { mutableStateOf("login") }
    var role by remember { mutableStateOf(window.localStorage.getItem("admin.role")) }
    var token by remember { mutableStateOf(window.localStorage.getItem("admin.token")) }
    var themePreference by remember {
        mutableStateOf(ThemePreference.fromStorage(window.localStorage.getItem("admin.theme")))
    }
    val isSystemDark = remember { window.matchMedia("(prefers-color-scheme: dark)").matches }
    val colorScheme = when (themePreference) {
        ThemePreference.LIGHT -> GreenLightColorScheme
        ThemePreference.DARK -> GreenDarkColorScheme
        ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when {
                role == null || token == null -> {
                    LoginScreen(
                        onLoggedIn = { resp ->
                            role = resp.user.role
                            token = resp.token
                            // persist session
                            role?.let { window.localStorage.setItem("admin.role", it) }
                            token?.let { window.localStorage.setItem("admin.token", it) }
                            if (role == "ADMIN") {
                                route = "plans"
                            }
                        },
                    )
                }

                role == "ADMIN" -> {
                    AdminMainScreen(
                        token = token!!,
                        currentRoute = route,
                        onRouteChange = { route = it },
                        onLogout = {
                            role = null
                            token = null
                            route = "login"
                            // clear persisted session
                            window.localStorage.removeItem("admin.role")
                            window.localStorage.removeItem("admin.token")
                        },
                        themePreference = themePreference,
                        onThemePreferenceChange = { preference ->
                            themePreference = preference
                            window.localStorage.setItem("admin.theme", preference.storageValue())
                        },
                    )
                }

                else -> { // todo: review for necessity
                    // Non-admin users redirected to login
                    Text(
                        "Admin access only. Please log in with an admin account.",
                        modifier = Modifier.padding(16.dp),
                    )
                    Button(
                        onClick = {
                            role = null
                            token = null
                            window.localStorage.removeItem("admin.role")
                            window.localStorage.removeItem("admin.token")
                        },
                    ) {
                        Text(stringResource(Res.string.log_out))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMainScreen(
    token: String,
    currentRoute: String,
    onRouteChange: (String) -> Unit,
    onLogout: () -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    var selectedTab by remember(currentRoute) {
        mutableIntStateOf(
            when (currentRoute) {
                "plans" -> 0
                "dishes" -> 1
                "mealplan" -> 2
                "customers" -> 3
                "settings" -> 4
                else -> 0
            },
        )
    }
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
                    onSelect = { i ->
                        selectedTab = i
                        val route = when (i) {
                            0 -> "plans"
                            1 -> "dishes"
                            2 -> "mealplan"
                            3 -> "customers"
                            4 -> "settings"
                            else -> "plans"
                        }
                        onRouteChange(route)
                    },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            var isViewingHistory by remember(selectedTab) { mutableStateOf(false) }
            when (selectedTab) {
                0 -> PlanScreen()

                1 -> DishScreen()

                2 -> if (isViewingHistory) {
                    DayScreen { isViewingHistory = false }
                    BackHandler { isViewingHistory = false }
                } else {
                    MealPlanDayScreen { isViewingHistory = true }
                }

                3 -> CustomerScreen(
                    token = token,
                    onChatOpenChanged = { isChatOpen = it },
                    onUnreadChanged = { hasUnreadChats = it },
                )

                4 -> SettingsScreen(
                    onLoggedOut = onLogout,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                )

                else -> PlanScreen()
            }
        }
    }
}
