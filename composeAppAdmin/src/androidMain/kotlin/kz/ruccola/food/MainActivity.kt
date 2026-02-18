package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.screens.AndroidCustomersScreen
import kz.ruccola.food.screens.AndroidDayScreen
import kz.ruccola.food.screens.AndroidDishScreen
import kz.ruccola.food.screens.AndroidMealPlanDayScreen
import kz.ruccola.food.screens.AndroidPlanScreen
import kz.ruccola.food.screens.LoginScreen
import kz.ruccola.food.screens.ProfileScreen
import kz.ruccola.food.screens.RegisterScreen
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.ui.LabeledNavigationBar
import kz.ruccola.food.ui.LabeledNavigationTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.applyStoredLanguage(this)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(colorScheme = GreenLightColorScheme) {
                var role by remember { mutableStateOf<String?>(null) }
                var token by remember { mutableStateOf<String?>(null) }
                var showRegister by remember { mutableStateOf(false) }

                if (role == null || token == null) {
                    if (showRegister) {
                        RegisterScreen(
                            onRegistered = { resp ->
                                role = resp.user.role
                                token = resp.token
                            },
                            onBackToLogin = { showRegister = false },
                        )
                    } else {
                        LoginScreen(
                            onLoggedIn = { resp ->
                                role = resp.user.role
                                token = resp.token
                            },
                            onGoToRegister = { showRegister = true },
                        )
                    }
                } else if (role == "ADMIN") {
                    var selectedTab by remember { mutableIntStateOf(0) }
                    Scaffold(
                        bottomBar = {
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
                        },
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
                            var isViewingHistory by remember(selectedTab) { mutableStateOf(false) }
                            when (selectedTab) {
                                0 -> {
                                    AndroidPlanScreen()
                                }

                                1 -> {
                                    AndroidDishScreen(adminToken = token)
                                }

                                2 -> {
                                    if (isViewingHistory) {
                                        AndroidDayScreen { isViewingHistory = false }
                                        BackHandler { isViewingHistory = false }
                                    } else {
                                        AndroidMealPlanDayScreen { isViewingHistory = true }
                                    }
                                }

                                3 -> {
                                    AndroidCustomersScreen(token = token!!)
                                }

                                4 -> {
                                    ProfileScreen(
                                        token = token!!,
                                        onLoggedOut = {
                                            role = null
                                            token = null
                                        },
                                    )
                                }

                                else -> {
                                    AndroidPlanScreen()
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
