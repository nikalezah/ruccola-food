package kz.ruccola.food.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kz.ruccola.food.ui.ResponsiveContainer
import kz.ruccola.food.ui.settings.LogoutButton
import kz.ruccola.food.ui.settings.ThemePicker
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    val scope = rememberCoroutineScope()

    ResponsiveContainer(maxContentWidth = 640.dp) {
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
                ThemePicker(
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    sectionTitle = stringResource(Res.string.theme_section_title),
                    systemLabel = stringResource(Res.string.theme_system),
                    lightLabel = stringResource(Res.string.theme_light),
                    darkLabel = stringResource(Res.string.theme_dark),
                )

                Spacer(modifier = Modifier.weight(1f))

                LogoutButton(
                    onClick = { scope.launch { onLoggedOut() } },
                    label = stringResource(Res.string.log_out),
                )
            }
        }
    }
}
