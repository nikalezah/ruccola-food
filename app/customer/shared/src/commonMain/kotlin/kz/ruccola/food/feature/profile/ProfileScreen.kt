package kz.ruccola.food.feature.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.tab_profile
import kz.ruccola.food.theme.ThemePreference
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    currentLanguage: String,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    onOpenWhatsApp: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tab_profile)) },
            )
        },
    ) { padding ->
        ProfileContent(
            uiState = uiState,
            currentLanguage = currentLanguage,
            themePreference = themePreference,
            onLanguageChanged = onLanguageChanged,
            onThemePreferenceChanged = onThemePreferenceChanged,
            onOpenWhatsApp = onOpenWhatsApp,
            onLogout = { viewModel.logout(onLoggedOut) },
            onEditClick = { viewModel.setEditing(true) },
            viewModel = viewModel,
            modifier = Modifier.padding(padding),
        )
    }
}
