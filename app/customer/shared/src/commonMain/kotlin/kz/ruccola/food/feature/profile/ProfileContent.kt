package kz.ruccola.food.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.contact_support
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.label_address
import food.composeappcustomer.generated.resources.label_email
import food.composeappcustomer.generated.resources.label_name
import food.composeappcustomer.generated.resources.language_section_title
import food.composeappcustomer.generated.resources.log_out
import food.composeappcustomer.generated.resources.theme_dark
import food.composeappcustomer.generated.resources.theme_light
import food.composeappcustomer.generated.resources.theme_section_title
import food.composeappcustomer.generated.resources.theme_system
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ToggleButtonsRow
import kz.ruccola.food.ui.settings.LogoutButton
import kz.ruccola.food.ui.settings.ThemePicker
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    currentLanguage: String,
    themePreference: ThemePreference,
    onLanguageChanged: (String) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    onOpenWhatsApp: () -> Unit,
    onLogout: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            uiState.isLoading && uiState.customer == null -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }

            uiState.error != null && uiState.customer == null -> {
                Text(
                    stringResource(Res.string.error_prefix, uiState.error ?: ""),
                    color = MaterialTheme.colorScheme.error,
                )
                LogoutButton(onClick = onLogout, label = stringResource(Res.string.log_out))
            }

            uiState.customer != null -> {
                val customer = uiState.customer
                OutlinedCard(modifier = Modifier.fillMaxWidth(), onClick = onEditClick) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(Res.string.label_email, customer.email))
                        Text(stringResource(Res.string.label_name, customer.firstName, customer.lastName))
                        if (customer.address.isNotBlank()) {
                            Text(stringResource(Res.string.label_address, customer.address))
                        }
                    }
                }

                if (uiState.isEditing) {
                    PersonalInfoEditDialog(
                        viewModel = viewModel,
                        initialFirstName = customer.firstName,
                        initialLastName = customer.lastName,
                        initialAddress = customer.address,
                    )
                }

                Spacer(Modifier.height(16.dp))
                ThemePicker(
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChanged,
                    sectionTitle = stringResource(Res.string.theme_section_title),
                    systemLabel = stringResource(Res.string.theme_system),
                    lightLabel = stringResource(Res.string.theme_light),
                    darkLabel = stringResource(Res.string.theme_dark),
                )

                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.language_section_title), style = MaterialTheme.typography.titleMedium)
                val languageCodes = listOf("kk", "ru", "en")
                val initialIndex = languageCodes.indexOf(currentLanguage)
                ToggleButtonsRow(
                    listOf("Қазақ", "Русский", "English"),
                    initialIndex,
                    onSelectedIndexChange = { onLanguageChanged(languageCodes[it]) },
                )

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onOpenWhatsApp) {
                    Icon(Icons.Outlined.ContactSupport, contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(Res.string.contact_support))
                }

                Spacer(Modifier.height(16.dp))
                LogoutButton(onClick = onLogout, label = stringResource(Res.string.log_out))
            }
        }
    }
}
