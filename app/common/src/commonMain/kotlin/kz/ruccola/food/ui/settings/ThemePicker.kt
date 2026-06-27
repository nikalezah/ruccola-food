package kz.ruccola.food.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.ToggleButtonsRow

@Composable
fun ThemePicker(
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    sectionTitle: String,
    systemLabel: String,
    lightLabel: String,
    darkLabel: String,
    modifier: Modifier = Modifier,
) {
    val themes = listOf(ThemePreference.SYSTEM, ThemePreference.LIGHT, ThemePreference.DARK)
    val themeIndex = themes.indexOf(themePreference).coerceAtLeast(0)
    Text(sectionTitle, style = MaterialTheme.typography.titleMedium, modifier = modifier)
    ToggleButtonsRow(
        options = listOf(systemLabel, lightLabel, darkLabel),
        initialSelectedIndex = themeIndex,
        onSelectedIndexChange = { onThemePreferenceChange(themes[it]) },
    )
}
