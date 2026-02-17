package kz.ruccola.food.customer.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.CustomerPlanDetailsDto
import kz.ruccola.food.api.CustomerUpdateDto
import kz.ruccola.food.api.PlanApi
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.customer.LocalStrings
import kz.ruccola.food.theme.ThemePreference
import kz.ruccola.food.web.common.ui.ToggleButtonsRow
import kotlin.math.roundToInt
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    token: String,
    onLoggedOut: () -> Unit,
    onLanguageChanged: (String) -> Unit = {},
    themePreference: ThemePreference,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    authApi: AuthApi = AuthApi(),
    planApi: PlanApi = PlanApi(),
) {
    val strings = LocalStrings.current
    var customer by remember { mutableStateOf<CustomerDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var customerPlan by remember { mutableStateOf<CustomerPlanDetailsDto?>(null) }
    var loadingPlan by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val customerApi = remember { CustomerApi() }

    LaunchedEffect(token) {
        loading = true
        error = null
        try {
            customer = customerApi.get(token)
            loadingPlan = true
            try {
                customerPlan = customerApi.getCustomerPlan(token)
            } catch (e: Exception) {
                // Plan not found is ok
            }
            loadingPlan = false
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    @Composable
    fun LogoutButton() {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    try {
                        authApi.logout(token)
                    } catch (e: Exception) {
                    }
                    onLoggedOut()
                }
            },
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
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
            var isEditing by remember { mutableStateOf(false) }
            when {
                loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }

                error != null -> {
                    Text(
                        strings.errorPrefix.replace("%s", error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                    )
                    LogoutButton()
                }

                customer != null -> {
                    var firstName by remember(customer) { mutableStateOf(customer!!.firstName) }
                    var lastName by remember(customer) { mutableStateOf(customer!!.lastName) }
                    var address by remember(customer) { mutableStateOf(customer!!.address) }

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isEditing = true },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(strings.labelEmail.replace("%s", customer!!.email))
                            Text(
                                strings.labelName
                                    .replaceFirst("%s", customer!!.firstName)
                                    .replaceFirst("%s", customer!!.lastName),
                            )
                            if (customer!!.address.isNotBlank()) {
                                Text(strings.labelAddress.replace("%s", customer!!.address))
                            }
                        }
                    }

                    // Chosen plan section
                    var showPlanDialog by remember { mutableStateOf(false) }
                    when {
                        loadingPlan -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(strings.loadingPlan)
                            }
                        }

                        customerPlan == null -> {
                            Text(strings.noPlanSelected)
                            Button(onClick = {
                                showPlanDialog = true
                            }) { Text(strings.choosePlan) }
                        }

                        else -> {
                            val plan = customerPlan!!.plan
                            val startDate = customerPlan!!.chosenDate
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
                                onClick = { showPlanDialog = true },
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

                    if (showPlanDialog) {
                        PlanSelectionDialog(
                            initialAllowVariants = customerPlan?.plan?.allowVariantChoice ?: false,
                            initialCalories = customerPlan?.plan?.calories?.amount,
                            initialDays = customerPlan?.plan?.periodDays?.amount,
                            planApi = planApi,
                            customersApi = customerApi,
                            token = token,
                            onDismiss = { showPlanDialog = false },
                            onSaved = { newDetails ->
                                customerPlan = newDetails
                                showPlanDialog = false
                            },
                        )
                    }

                    if (isEditing) {
                        var saving by remember { mutableStateOf(false) }
                        var saveError by remember { mutableStateOf<String?>(null) }
                        AlertDialog(
                            onDismissRequest = { isEditing = false },
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

                                    if (saveError != null) {
                                        Text(
                                            strings.errorPrefix.replace("%s", saveError!!),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            saving = true
                                            saveError = null
                                            try {
                                                val updated = customerApi.update(
                                                    token,
                                                    CustomerUpdateDto(
                                                        firstName = firstName.trim(),
                                                        lastName = lastName.trim(),
                                                        address = address.trim(),
                                                    ),
                                                )
                                                customer = updated
                                                isEditing = false
                                            } catch (t: Throwable) {
                                                saveError = t.message ?: strings.saveFailed
                                            } finally {
                                                saving = false
                                            }
                                        }
                                    },
                                    enabled = !saving,
                                ) {
                                    Text(if (saving) strings.saving else strings.save)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { isEditing = false },
                                    enabled = !saving,
                                ) {
                                    Text(strings.cancel)
                                }
                            },
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
                    val currentLang = window.localStorage.getItem("customer.language") ?: "ru"
                    val initialIndex = when (currentLang) {
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
private fun PlanSelectionDialog(
    initialAllowVariants: Boolean,
    initialCalories: Int?,
    initialDays: Int?,
    planApi: PlanApi,
    customersApi: CustomerApi,
    token: String,
    onDismiss: () -> Unit,
    onSaved: (CustomerPlanDetailsDto) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var allowVariants by remember { mutableStateOf(initialAllowVariants) }

    var caloriesOptions by remember { mutableStateOf<List<Int>>(emptyList()) }
    var caloriesIndex by remember { mutableFloatStateOf(0f) }
    var daysOptions by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    var allPlans by remember { mutableStateOf<List<PlanDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val firstLoad = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            allPlans = planApi.getAll()
        } catch (e: Exception) {
            // CancellationException occurs when sliding with the mouse cursor cancels the previous coroutine
            // So it shouldn't be displayed
            if (e is CancellationException) throw e
            error = e.message
        }
        loading = false
    }

    suspend fun loadCaloriesAndDays(preserveDay: Boolean) {
        error = null
        try {
            val oldCalories = caloriesOptions.getOrNull(caloriesIndex.toInt())
            val opts = planApi.getAvailableCalories(allowVariants)
            val targetCalories =
                if (firstLoad.value) {
                    initialCalories
                } else {
                    oldCalories
                }
            caloriesOptions = opts

            val idx = targetCalories?.let { opts.indexOf(it) }?.takeIf { it >= 0 } ?: 0
            caloriesIndex = idx.toFloat()

            val c = opts.getOrNull(idx)
            if (c != null) {
                try {
                    val oldDays = selectedDayIndex?.let { daysOptions.getOrNull(it) }
                    val ds = planApi.getAvailableDays(allowVariants, c)
                    val targetDays =
                        if (firstLoad.value) {
                            initialDays
                        } else if (preserveDay) {
                            oldDays
                        } else {
                            null
                        }
                    daysOptions = ds
                    val dIdx = targetDays?.let { ds.indexOf(it) }?.takeIf { it >= 0 }
                        ?: if (ds.isNotEmpty()) 0 else null
                    selectedDayIndex = dIdx
                } catch (e: Exception) {
                    // CancellationException occurs when sliding with the mouse cursor cancels the previous coroutine
                    // So it shouldn't be displayed
                    if (e is CancellationException) throw e
                    error = e.message
                    daysOptions = emptyList()
                    selectedDayIndex = null
                }
            } else {
                daysOptions = emptyList()
                selectedDayIndex = null
            }
        } catch (e: Exception) {
            // CancellationException occurs when sliding with the mouse cursor cancels the previous coroutine
            // So it shouldn't be displayed
            if (e is CancellationException) throw e
            error = e.message
        }
        firstLoad.value = false
    }

    LaunchedEffect(allowVariants) { loadCaloriesAndDays(preserveDay = true) }
    LaunchedEffect(caloriesIndex) {
        if (!firstLoad.value) {
            val c = caloriesOptions.getOrNull(caloriesIndex.toInt())
            if (c != null) {
                try {
                    val oldDays = selectedDayIndex?.let { daysOptions.getOrNull(it) }
                    val ds = planApi.getAvailableDays(allowVariants, c)
                    daysOptions = ds
                    selectedDayIndex = oldDays?.let { ds.indexOf(it) }?.takeIf { it >= 0 }
                        ?: if (ds.isNotEmpty()) 0 else null
                } catch (e: Exception) {
                    // CancellationException occurs when sliding with the mouse cursor cancels the previous coroutine
                    // So it shouldn't be displayed
                    if (e is CancellationException) throw e
                    error = e.message
                }
            } else {
                daysOptions = emptyList()
                selectedDayIndex = null
            }
        }
    }

    val selectedCalories = caloriesOptions.getOrNull(caloriesIndex.toInt())
    val selectedDays = selectedDayIndex?.let { idx -> daysOptions.getOrNull(idx) }
    val matchingPlan = remember(allowVariants, selectedCalories, selectedDays, allPlans) {
        if (selectedCalories == null || selectedDays == null) {
            null
        } else {
            allPlans.firstOrNull { p ->
                p.allowVariantChoice == allowVariants &&
                    p.calories.amount == selectedCalories &&
                    p.periodDays.amount == selectedDays
            }
        }
    }
    val totalPrice = matchingPlan?.let { it.pricePerDay * (selectedDays ?: 0) }

    var saving by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.choosePlan, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleButtonsRow(
                    options = listOf(strings.noVariants, strings.withVariants),
                    initialSelectedIndex = if (allowVariants) 1 else 0,
                    onSelectedIndexChange = { i: Int -> allowVariants = i == 1 },
                )

                if (caloriesOptions.isNotEmpty()) {
                    val maxIndex = (caloriesOptions.size - 1).coerceAtLeast(0)
                    val steps = if (maxIndex >= 1) maxIndex - 1 else 0
                    val shownCalories = caloriesOptions.getOrNull(caloriesIndex.toInt())
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val caloriesLabel = shownCalories?.toString() ?: "-"
                        Text(strings.labelCalories.replace("%s", caloriesLabel))
                        Slider(
                            value = caloriesIndex,
                            onValueChange = { caloriesIndex = it.roundToInt().toFloat() },
                            valueRange = 0f..maxIndex.toFloat(),
                            steps = steps,
                        )
                    }
                } else if (!loading) {
                    Text(strings.noPlansAvailable)
                }

                if (daysOptions.isNotEmpty()) {
                    Text(strings.periodDays)
                    ToggleButtonsRow(
                        options = daysOptions.map { it.toString() },
                        initialSelectedIndex = selectedDayIndex ?: 0,
                        onSelectedIndexChange = { i: Int -> selectedDayIndex = i },
                    )
                }

                val pricePerDayText = matchingPlan?.pricePerDay?.toString() ?: "-"
                val totalPriceText = totalPrice?.toString() ?: "-"
                Text(strings.labelPricePerDay.replace("%s", pricePerDayText))
                Text(strings.labelTotalPrice.replace("%s", totalPriceText))

                if (error != null) {
                    Text(strings.errorPrefix.replace("%s", error!!), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (matchingPlan != null && selectedDays != null) {
                        scope.launch {
                            saving = true
                            error = null
                            try {
                                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
//                                val today = LocalDate.parse(kotlin.js.Date().toISOString().split("T")[0])
                                val saved = customersApi.saveCustomerPlan(
                                    token,
                                    CustomerPlanCreateDto(planId = matchingPlan.id, chosenDate = today),
                                )
                                onSaved(saved)
                            } catch (t: Throwable) {
                                error = t.message ?: strings.saveFailed
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
                enabled = matchingPlan != null && !saving,
            ) {
                Text(if (saving) strings.saving else strings.save)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !saving) { Text(strings.cancel) }
        },
    )
}
