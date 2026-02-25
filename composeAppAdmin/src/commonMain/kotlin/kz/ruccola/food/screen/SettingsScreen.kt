package kz.ruccola.food.screen

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
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.log_out
import food.composeappadmin.generated.resources.screen_settings_title
import food.composeappadmin.generated.resources.theme_dark
import food.composeappadmin.generated.resources.theme_light
import food.composeappadmin.generated.resources.theme_section_title
import food.composeappadmin.generated.resources.theme_system
import kotlinx.coroutines.launch
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.ToggleButtonsRow
import org.jetbrains.compose.resources.stringResource

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
                title = { Text(stringResource(Res.string.screen_settings_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.theme_section_title), style = MaterialTheme.typography.titleMedium)
            val themeOptions = listOf(
                stringResource(Res.string.theme_system),
                stringResource(Res.string.theme_light),
                stringResource(Res.string.theme_dark),
            )
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
                Text(stringResource(Res.string.log_out))
            }
        }
    }
}
