package kz.ruccola.food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.screens.AndroidChatScreen
import kz.ruccola.food.screens.AndroidCustomerWeekScreen
import kz.ruccola.food.screens.LoginScreen
import kz.ruccola.food.screens.ProfileScreen
import kz.ruccola.food.screens.RegisterScreen
import kz.ruccola.food.theme.GreenLightColorScheme
import kz.ruccola.food.ui.LabeledNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.applyStoredLanguage(this)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(colorScheme = GreenLightColorScheme) {
                var token by remember { mutableStateOf<String?>(null) }
                var showRegister by remember { mutableStateOf(false) }

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
                    Scaffold(
                        bottomBar = {
                            LabeledNavigationBar(
                                tabs = listOf(
                                    Triple(
                                        Icons.Filled.DinnerDining,
                                        Icons.Outlined.DinnerDining,
                                        stringResource(R.string.tab_dishes),
                                    ),
                                    Triple(
                                        Icons.AutoMirrored.Filled.Chat,
                                        Icons.AutoMirrored.Outlined.Chat,
                                        stringResource(R.string.tab_chat),
                                    ),
                                    Triple(
                                        Icons.Filled.ManageAccounts,
                                        Icons.Outlined.ManageAccounts,
                                        stringResource(R.string.tab_profile),
                                    ),
                                ),
                                selected = { selectedTab },
                                onSelect = { selectedTab = it },
                            )
                        },
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
                            when (selectedTab) {
                                0 -> AndroidCustomerWeekScreen(token = token!!)

                                1 -> AndroidChatScreen(token = token!!)

                                2 -> ProfileScreen(
                                    token = token!!,
                                    onLoggedOut = { token = null },
                                )

                                else -> AndroidCustomerWeekScreen(token = token!!)
                            }
                        }
                    }
                }
            }
        }
    }
}
