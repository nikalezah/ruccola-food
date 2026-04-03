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
import androidx.compose.material3.Switch
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
import food.composeappcustomer.generated.resources.choose_plan
import food.composeappcustomer.generated.resources.chosen_plan_title
import food.composeappcustomer.generated.resources.days_quantity
import food.composeappcustomer.generated.resources.delivery_prefs_title
import food.composeappcustomer.generated.resources.edit_personal_info_title
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.evening
import food.composeappcustomer.generated.resources.first_name
import food.composeappcustomer.generated.resources.format_kcal
import food.composeappcustomer.generated.resources.label_address
import food.composeappcustomer.generated.resources.label_calories
import food.composeappcustomer.generated.resources.label_email
import food.composeappcustomer.generated.resources.label_end_date
import food.composeappcustomer.generated.resources.label_name
import food.composeappcustomer.generated.resources.label_price_per_day
import food.composeappcustomer.generated.resources.label_start_date
import food.composeappcustomer.generated.resources.label_total_price
import food.composeappcustomer.generated.resources.language_section_title
import food.composeappcustomer.generated.resources.last_name
import food.composeappcustomer.generated.resources.loading_plan
import food.composeappcustomer.generated.resources.log_out
import food.composeappcustomer.generated.resources.morning
import food.composeappcustomer.generated.resources.morning_delivery
import food.composeappcustomer.generated.resources.needs_cutlery
import food.composeappcustomer.generated.resources.no_plan_selected
import food.composeappcustomer.generated.resources.no_plans_available
import food.composeappcustomer.generated.resources.period_days
import food.composeappcustomer.generated.resources.save
import food.composeappcustomer.generated.resources.saving
import food.composeappcustomer.generated.resources.screen_profile_title
import food.composeappcustomer.generated.resources.theme_dark
import food.composeappcustomer.generated.resources.theme_light
import food.composeappcustomer.generated.resources.theme_section_title
import food.composeappcustomer.generated.resources.theme_system
import food.composeappcustomer.generated.resources.weekend_delivery
import kotlinx.datetime.LocalDate
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ToggleButtonsRow
import kz.ruccola.food.viewmodel.ProfileViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

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
                title = { Text(stringResource(Res.string.screen_profile_title)) },
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

                    // Chosen plan section
                    when {
                        uiState.isLoadingPlan -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(stringResource(Res.string.loading_plan))
                            }
                        }

                        uiState.customerPlan == null -> {
                            Text(stringResource(Res.string.no_plan_selected))
                            Button(onClick = {
                                viewModel.setShowPlanDialog(true)
                            }) { Text(stringResource(Res.string.choose_plan)) }
                        }

                        else -> {
                            val customerPlan = uiState.customerPlan!!
                            val startDate = customerPlan.chosenDate
                            val days = customerPlan.days
                            val endEpoch = startDate.toEpochDays() + (days - 1)
                            val endDate = LocalDate.fromEpochDays(endEpoch)
                            val totalPrice = customerPlan.pricePerDay * days

                            val kcalText = stringResource(Res.string.format_kcal, customerPlan.calories.toString())
                            val totalPriceText = totalPrice.toString()
                            val startDateText = startDate.toString()
                            val endDateText = endDate.toString()

                            val daysText = pluralStringResource(Res.plurals.days_quantity, days, days)

                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.setShowPlanDialog(true) },
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        stringResource(Res.string.chosen_plan_title),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(stringResource(Res.string.label_calories, kcalText))
                                    Text(daysText)
                                    Text(stringResource(Res.string.label_total_price, totalPriceText))
                                    Text(stringResource(Res.string.label_start_date, startDateText))
                                    Text(stringResource(Res.string.label_end_date, endDateText))
                                }
                            }
                        }
                    }

                    if (uiState.showPlanDialog) {
                        PlanSelectionDialog(
                            viewModel = viewModel,
                        )
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
                    Text(
                        stringResource(Res.string.delivery_prefs_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.needs_cutlery))
                        Switch(
                            checked = uiState.needsCutlery,
                            onCheckedChange = {
                                viewModel.updateDeliveryPrefs(needsCutlery = it)
                            },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.weekend_delivery))
                        Switch(
                            checked = uiState.weekendDelivery,
                            onCheckedChange = {
                                viewModel.updateDeliveryPrefs(weekendDelivery = it)
                            },
                        )
                    }
                    Text(
                        stringResource(Res.string.morning_delivery),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    ToggleButtonsRow(
                        listOf(
                            stringResource(Res.string.morning),
                            stringResource(Res.string.evening),
                        ),
                        if (uiState.morningDelivery) 0 else 1,
                        onSelectedIndexChange = { i ->
                            viewModel.updateDeliveryPrefs(morningDelivery = i == 0)
                        },
                    )

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

@Composable
private fun PlanSelectionDialog(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.setShowPlanDialog(false) },
        title = { Text(stringResource(Res.string.choose_plan), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.caloriesOptions.isNotEmpty()) {
                    val maxIndex = (uiState.caloriesOptions.size - 1).coerceAtLeast(0)
                    val steps = if (maxIndex >= 1) maxIndex - 1 else 0
                    val shownCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val caloriesLabel = shownCalories?.toString() ?: "-"
                        Text(stringResource(Res.string.label_calories, caloriesLabel))
                        Slider(
                            value = uiState.caloriesIndex.toFloat(),
                            onValueChange = { viewModel.setCaloriesIndex(it.roundToInt()) },
                            valueRange = 0f..maxIndex.toFloat(),
                            steps = steps,
                        )
                    }
                } else if (!uiState.isLoadingPlansForDialog) {
                    Text(stringResource(Res.string.no_plans_available))
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${stringResource(Res.string.period_days)}: ${uiState.selectedDays}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(
                        value = uiState.selectedDays.toFloat(),
                        onValueChange = { viewModel.setSelectedDays(it.roundToInt()) },
                        valueRange = 1f..30f,
                        steps = 28,
                    )
                }

                val pricePerDay = viewModel.effectivePricePerDay()
                val totalPrice = pricePerDay?.let { it * uiState.selectedDays }

                val pricePerDayText = pricePerDay?.toString() ?: "-"
                val totalPriceText = totalPrice?.toString() ?: "-"
                Text(stringResource(Res.string.label_price_per_day, pricePerDayText))
                Text(stringResource(Res.string.label_total_price, totalPriceText))

                if (uiState.dialogError != null) {
                    Text(
                        stringResource(Res.string.error_prefix, uiState.dialogError!!),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            val selectedCalories = uiState.caloriesOptions.getOrNull(uiState.caloriesIndex)
            val hasValidPlan = selectedCalories != null && uiState.allPlans.any {
                it.calories.amount == selectedCalories && it.periodDays.amount <= uiState.selectedDays
            }

            Button(
                onClick = { viewModel.savePlan() },
                enabled = hasValidPlan && !uiState.isSavingPlan,
            ) {
                Text(if (uiState.isSavingPlan) stringResource(Res.string.saving) else stringResource(Res.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.setShowPlanDialog(false) },
                enabled = !uiState.isSavingPlan,
            ) { Text(stringResource(Res.string.cancel)) }
        },
    )
}
