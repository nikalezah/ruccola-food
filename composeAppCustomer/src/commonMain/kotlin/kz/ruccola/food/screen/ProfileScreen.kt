package kz.ruccola.food.screen

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.address
import food.composeappcustomer.generated.resources.cancel
import food.composeappcustomer.generated.resources.edit_personal_info_title
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.first_name
import food.composeappcustomer.generated.resources.label_address
import food.composeappcustomer.generated.resources.label_email
import food.composeappcustomer.generated.resources.label_name
import food.composeappcustomer.generated.resources.language_section_title
import food.composeappcustomer.generated.resources.last_name
import food.composeappcustomer.generated.resources.log_out
import food.composeappcustomer.generated.resources.save
import food.composeappcustomer.generated.resources.saving
import food.composeappcustomer.generated.resources.tab_profile
import food.composeappcustomer.generated.resources.theme_dark
import food.composeappcustomer.generated.resources.theme_light
import food.composeappcustomer.generated.resources.theme_section_title
import food.composeappcustomer.generated.resources.theme_system
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ToggleButtonsRow
import kz.ruccola.food.viewmodel.ProfileViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    currentLanguage: String,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    viewModel: ProfileViewModel = viewModel { ProfileViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    @Composable
    fun LogoutButton() {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.logout(onLoggedOut) },
        ) {
            Icon(Icons.Filled.Logout, contentDescription = "Logout")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.log_out))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tab_profile)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }

                uiState.error != null -> {
                    Text(
                        stringResource(Res.string.error_prefix, uiState.error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                    )
                    LogoutButton()
                }

                uiState.customer != null -> {
                    val customer = uiState.customer!!
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.setEditing(true) },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(Res.string.label_email, customer.email))
                            Text(
                                stringResource(Res.string.label_name, customer.firstName, customer.lastName),
                            )
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
                    Text(stringResource(Res.string.theme_section_title), style = MaterialTheme.typography.titleMedium)
                    val themeIndex = when (themePreference) {
                        ThemePreference.SYSTEM -> 0
                        ThemePreference.LIGHT -> 1
                        ThemePreference.DARK -> 2
                    }
                    ToggleButtonsRow(
                        listOf(
                            stringResource(Res.string.theme_system),
                            stringResource(Res.string.theme_light),
                            stringResource(Res.string.theme_dark),
                        ),
                        themeIndex,
                        onSelectedIndexChange = { i ->
                            val newPreference = when (i) {
                                1 -> ThemePreference.LIGHT
                                2 -> ThemePreference.DARK
                                else -> ThemePreference.SYSTEM
                            }
                            onThemePreferenceChanged(newPreference)
                        },
                    )

                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(Res.string.language_section_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val languages = listOf("English", "Русский", "Қазақ")
                    val initialIndex = when (currentLanguage) {
                        "en" -> 0
                        "ru" -> 1
                        "kk" -> 2
                        else -> 1
                    }
                    ToggleButtonsRow(
                        languages,
                        initialIndex,
                        onSelectedIndexChange = { i ->
                            val newLang = when (i) {
                                0 -> "en"
                                1 -> "ru"
                                2 -> "kk"
                                else -> "ru"
                            }
                            onLanguageChanged(newLang)
                        },
                    )

                    Spacer(Modifier.height(16.dp))
                    LogoutButton()
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoEditDialog(
    viewModel: ProfileViewModel,
    initialFirstName: String,
    initialLastName: String,
    initialAddress: String,
) {
    val uiState by viewModel.uiState.collectAsState()

    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var address by remember { mutableStateOf(initialAddress) }

    AlertDialog(
        onDismissRequest = { viewModel.setEditing(false) },
        title = {
            Text(
                stringResource(Res.string.edit_personal_info_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(stringResource(Res.string.first_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(stringResource(Res.string.last_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringResource(Res.string.address)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.saveError != null) {
                    Text(
                        stringResource(Res.string.error_prefix, uiState.saveError!!),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateCustomer(firstName, lastName, address)
                },
                enabled = !uiState.isSaving,
            ) {
                Text(if (uiState.isSaving) stringResource(Res.string.saving) else stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.setEditing(false) },
                enabled = !uiState.isSaving,
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
