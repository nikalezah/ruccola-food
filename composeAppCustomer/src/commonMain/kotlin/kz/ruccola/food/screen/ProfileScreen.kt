package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import kotlinx.datetime.LocalDate
import kz.ruccola.food.LocalStrings
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ToggleButtonsRow
import kz.ruccola.food.viewmodel.ProfileViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    token: String,
    onLoggedOut: () -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    currentLanguage: String,
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    viewModel: ProfileViewModel = viewModel { ProfileViewModel() },
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(token) {
        viewModel.loadProfile(token)
    }

    @Composable
    fun LogoutButton() {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.logout(token, onLoggedOut) },
        ) {
            Icon(Icons.Filled.Logout, contentDescription = "Logout")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(strings.logOut)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.screenProfileTitle) },
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
                        strings.errorPrefix.replace("%s", uiState.error ?: ""),
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
                            Text(strings.labelEmail.replace("%s", customer.email))
                            Text(
                                strings.labelName
                                    .replaceFirst("%s", customer.firstName)
                                    .replaceFirst("%s", customer.lastName),
                            )
                            if (customer.address.isNotBlank()) {
                                Text(strings.labelAddress.replace("%s", customer.address))
                            }
                        }
                    }

                    // Chosen plan section
                    when {
                        uiState.isLoadingPlan -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(strings.loadingPlan)
                            }
                        }

                        uiState.customerPlan == null -> {
                            Text(strings.noPlanSelected)
                            Button(onClick = {
                                viewModel.setShowPlanDialog(true)
                            }) { Text(strings.choosePlan) }
                        }

                        else -> {
                            val customerPlan = uiState.customerPlan!!
                            val plan = customerPlan.plan
                            val startDate = customerPlan.chosenDate
                            val days = plan.periodDays.amount
                            val endEpoch = startDate.toEpochDays() + (days - 1)
                            val endDate = LocalDate.fromEpochDays(endEpoch)
                            val totalPrice = plan.pricePerDay * days

                            val kcalText = strings.formatKcal.replace("%s", plan.calories.amount.toString())
                            val totalPriceText = totalPrice.toString()
                            val startDateText = startDate.toString()
                            val endDateText = endDate.toString()

                            val daysText = when {
                                days % 10 == 1 && days % 100 != 11 -> strings.daysQuantityOne.replace(
                                    "%d",
                                    days.toString(),
                                )

                                days % 10 in 2..4 && days % 100 !in 12..14 -> strings.daysQuantityFew.replace(
                                    "%d",
                                    days.toString(),
                                )

                                else -> strings.daysQuantityMany.replace("%d", days.toString())
                            }

                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.setShowPlanDialog(true) },
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        strings.chosenPlanTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(strings.labelCalories.replace("%s", kcalText))
                                    Text(daysText)
                                    Text(strings.labelTotalPrice.replace("%s", totalPriceText))
                                    Text(strings.labelStartDate.replace("%s", startDateText))
                                    Text(strings.labelEndDate.replace("%s", endDateText))
                                }
                            }
                        }
                    }

                    if (uiState.showPlanDialog) {
                        PlanSelectionDialog(
                            token = token,
                            viewModel = viewModel,
                        )
                    }

                    if (uiState.isEditing) {
                        PersonalInfoEditDialog(
                            token = token,
                            viewModel = viewModel,
                            initialFirstName = customer.firstName,
                            initialLastName = customer.lastName,
                            initialAddress = customer.address,
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(strings.themeSectionTitle, style = MaterialTheme.typography.titleMedium)
                    val themeIndex = when (themePreference) {
                        ThemePreference.SYSTEM -> 0
                        ThemePreference.LIGHT -> 1
                        ThemePreference.DARK -> 2
                    }
                    ToggleButtonsRow(
                        listOf(strings.themeSystem, strings.themeLight, strings.themeDark),
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
                    Text(strings.languageSectionTitle, style = MaterialTheme.typography.titleMedium)
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
    token: String,
    viewModel: ProfileViewModel,
    initialFirstName: String,
    initialLastName: String,
    initialAddress: String,
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()

    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var address by remember { mutableStateOf(initialAddress) }

    AlertDialog(
        onDismissRequest = { viewModel.setEditing(false) },
        title = {
            Text(
                strings.editPersonalInfoTitle,
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
                    label = { Text(strings.firstName) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(strings.lastName) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.address) },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.saveError != null) {
                    Text(
                        strings.errorPrefix.replace("%s", uiState.saveError!!),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateCustomer(token, firstName, lastName, address)
                },
                enabled = !uiState.isSaving,
            ) {
                Text(if (uiState.isSaving) strings.saving else strings.save)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.setEditing(false) },
                enabled = !uiState.isSaving,
            ) {
                Text(strings.cancel)
            }
        },
    )
}

@Composable
private fun PlanSelectionDialog(
    token: String,
    viewModel: ProfileViewModel,
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.setShowPlanDialog(false) },
        title = { Text(strings.choosePlan, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleButtonsRow(
                    options = listOf(strings.noVariants, strings.withVariants),
                    initialSelectedIndex = if (uiState.allowVariants) 1 else 0,
                    onSelectedIndexChange = { i: Int -> viewModel.setAllowVariants(i == 1) },
                )

                if (uiState.caloriesOptions.isNotEmpty()) {
                    val maxIndex = (uiState.caloriesOptions.size - 1).coerceAtLeast(0)
                    val steps = if (maxIndex >= 1) maxIndex - 1 else 0
                    val shownCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val caloriesLabel = shownCalories?.toString() ?: "-"
                        Text(strings.labelCalories.replace("%s", caloriesLabel))
                        Slider(
                            value = uiState.caloriesIndex.toFloat(),
                            onValueChange = { viewModel.setCaloriesIndex(it.roundToInt()) },
                            valueRange = 0f..maxIndex.toFloat(),
                            steps = steps,
                        )
                    }
                } else if (!uiState.isLoadingPlansForDialog) {
                    Text(strings.noPlansAvailable)
                }

                if (uiState.daysOptions.isNotEmpty()) {
                    Text(strings.periodDays)
                    ToggleButtonsRow(
                        options = uiState.daysOptions.map { it.toString() },
                        initialSelectedIndex = uiState.selectedDayIndex ?: 0,
                        onSelectedIndexChange = { i: Int -> viewModel.setSelectedDayIndex(i) },
                    )
                }

                val selectedCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)
                val selectedDays = uiState.selectedDayIndex?.let { uiState.daysOptions.getOrNull(it) }
                val matchingPlan = if (selectedCalories == null || selectedDays == null) {
                    null
                } else {
                    uiState.allPlans.firstOrNull { p ->
                        p.allowVariantChoice == uiState.allowVariants &&
                            p.calories.amount == selectedCalories &&
                            p.periodDays.amount == selectedDays
                    }
                }
                val totalPrice = matchingPlan?.let { it.pricePerDay * (selectedDays ?: 0) }

                val pricePerDayText = matchingPlan?.pricePerDay?.toString() ?: "-"
                val totalPriceText = totalPrice?.toString() ?: "-"
                Text(strings.labelPricePerDay.replace("%s", pricePerDayText))
                Text(strings.labelTotalPrice.replace("%s", totalPriceText))

                if (uiState.dialogError != null) {
                    Text(
                        strings.errorPrefix.replace("%s", uiState.dialogError!!),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            val selectedDays = uiState.selectedDayIndex?.let { uiState.daysOptions.getOrNull(it) }
            val selectedCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)
            val matchingPlan = if (selectedCalories == null || selectedDays == null) {
                null
            } else {
                uiState.allPlans.firstOrNull { p ->
                    p.allowVariantChoice == uiState.allowVariants &&
                        p.calories.amount == selectedCalories &&
                        p.periodDays.amount == selectedDays
                }
            }

            Button(
                onClick = { viewModel.savePlan(token) },
                enabled = matchingPlan != null && !uiState.isSavingPlan,
            ) {
                Text(if (uiState.isSavingPlan) strings.saving else strings.save)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.setShowPlanDialog(false) },
                enabled = !uiState.isSavingPlan,
            ) { Text(strings.cancel) }
        },
    )
}
