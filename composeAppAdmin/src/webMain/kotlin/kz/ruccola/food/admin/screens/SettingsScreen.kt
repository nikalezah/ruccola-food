package kz.ruccola.food.admin.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.web.common.ui.ToggleButtonsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(Strings.screenSettingsTitle) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(Strings.themeSectionTitle, style = MaterialTheme.typography.titleMedium)
            val themeOptions = listOf(Strings.themeSystem, Strings.themeLight, Strings.themeDark)
            val themeIndex = when (themePreference) {
                ThemePreference.SYSTEM -> 0
                ThemePreference.LIGHT -> 1
                ThemePreference.DARK -> 2
            }
            ToggleButtonsRow(
                options = themeOptions,
                initialSelectedIndex = themeIndex,
                onSelectedIndexChange = { index ->
                    val newPreference = when (index) {
                        1 -> ThemePreference.LIGHT
                        2 -> ThemePreference.DARK
                        else -> ThemePreference.SYSTEM
                    }
                    onThemePreferenceChange(newPreference)
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { scope.launch { onLoggedOut() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Strings.logOut)
            }
        }
    }
}
