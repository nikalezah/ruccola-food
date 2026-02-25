package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.localization.AppPreferences
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
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.applyStoredLanguage(this)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val themePrefString by AppPreferences.themeFlow(context).collectAsState(initial = null)
            val themePreference = ThemePreference.fromStorage(themePrefString)

            val isSystemDark = isSystemInDarkTheme()
            val colorScheme = when (themePreference) {
                ThemePreference.LIGHT -> GreenLightColorScheme
                ThemePreference.DARK -> GreenDarkColorScheme
                ThemePreference.SYSTEM -> if (isSystemDark) GreenDarkColorScheme else GreenLightColorScheme
            }

            MaterialTheme(colorScheme = colorScheme) {
                var role by remember { mutableStateOf<String?>(null) }
                var token by remember { mutableStateOf<String?>(null) }

                if (role == null || token == null) {
                    var loginLoading by remember { mutableStateOf(true) }
                    val loginRolePref by AppPreferences.roleFlow(context).collectAsState(initial = null)
                    val loginTokenPref by AppPreferences.tokenFlow(context).collectAsState(initial = null)

                    androidx.compose.runtime.LaunchedEffect(loginRolePref, loginTokenPref) {
                        if (loginRolePref != null && loginTokenPref != null) {
                            role = loginRolePref
                            token = loginTokenPref
                        }
                        loginLoading = false
                    }

                    if (loginLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    } else {
                        LoginScreen(
                            onLoggedIn = { resp ->
                                role = resp.user.role
                                token = resp.token
                                scope.launch {
                                    AppPreferences.setRole(context, role)
                                    AppPreferences.setToken(context, token)
                                }
                            },
                        )
                    }
                } else if (role == "ADMIN") {
                    var selectedTab by remember { mutableIntStateOf(0) }
                    var isChatOpen by remember { mutableStateOf(false) }
                    Scaffold(
                        bottomBar = {
                            if (!isChatOpen) {
                                LabeledNavigationBar(
                                    tabs = listOf(
                                        LabeledNavigationTab(
                                            Icons.Filled.PriceChange,
                                            Icons.Outlined.PriceChange,
                                            "Цены",
                                        ),
                                        LabeledNavigationTab(
                                            Icons.Filled.DinnerDining,
                                            Icons.Outlined.DinnerDining,
                                            "Блюда",
                                        ),
                                        LabeledNavigationTab(
                                            Icons.Filled.CalendarMonth,
                                            Icons.Outlined.CalendarMonth,
                                            "Расписание",
                                        ),
                                        LabeledNavigationTab(
                                            Icons.Filled.Groups,
                                            Icons.Outlined.Groups,
                                            "Клиенты",
                                        ),
                                        LabeledNavigationTab(
                                            Icons.Filled.Settings,
                                            Icons.Outlined.Settings,
                                            "Настройки",
                                        ),
                                    ),
                                    selected = { selectedTab },
                                    onSelect = { selectedTab = it },
                                )
                            }
                        },
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
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
                                        token = token!!,
                                        onChatOpenChanged = { isChatOpen = it },
                                    )
                                }

                                4 -> {
                                    SettingsScreen(
                                        onLoggedOut = {
                                            role = null
                                            token = null
                                            scope.launch {
                                                AppPreferences.setRole(context, null)
                                                AppPreferences.setToken(context, null)
                                            }
                                        },
                                        themePreference = themePreference,
                                        onThemePreferenceChange = { newPref ->
                                            scope.launch {
                                                AppPreferences.setTheme(context, newPref.storageValue())
                                            }
                                        },
                                    )
                                }

                                else -> {
                                    PlanScreen()
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    ) {
                        Text(
                            text = "Admin access only. Please log in with an admin account.",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Button(
                            onClick = {
                                role = null
                                token = null
                                scope.launch {
                                    AppPreferences.setRole(context, null)
                                    AppPreferences.setToken(context, null)
                                }
                            },
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
                            Text("Log out")
                        }
                    }
                }
            }
        }
    }
}
