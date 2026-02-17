package kz.ruccola.food.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kz.ruccola.food.R
import kz.ruccola.food.api.AuthApi
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.CustomerPlanDetailsDto
import kz.ruccola.food.api.CustomerUpdateDto
import kz.ruccola.food.api.PlanApi
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.ui.ToggleButtonsRow
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    token: String,
    onLoggedOut: () -> Unit,
    authApi: AuthApi = AuthApi(),
    planApi: PlanApi = PlanApi(),
) {
    var customer by remember { mutableStateOf<CustomerDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var customerPlan by remember { mutableStateOf<CustomerPlanDetailsDto?>(null) }
    var loadingPlan by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val customerApi = remember { CustomerApi() }

    LaunchedEffect(token) {
        loading = true
        error = null
        runCatching { customerApi.get(token) }
            .onSuccess { c ->
                customer = c
                // Load customer plan if user is a customer
                loadingPlan = true
                runCatching { customerApi.getCustomerPlan(token) }
                    .onSuccess { customerPlan = it }
                    .onFailure { /* Plan not found is ok */ }
                loadingPlan = false
            }
            .onFailure { error = it.message }
        loading = false
    }

    @Composable
    fun LogoutButton() {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    runCatching { authApi.logout(token) }
                    onLoggedOut()
                }
            },
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.log_out))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.screen_profile_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp, 0.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            var isEditing by remember { mutableStateOf(false) }
            when {
                loading -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Text(
                        stringResource(R.string.error_prefix, error ?: ""),
                        color = MaterialTheme.colorScheme.error,
                    )
                    LogoutButton()
                }

                customer != null -> {
                    var firstName by remember(customer) { mutableStateOf(customer!!.firstName) }
                    var lastName by remember(customer) { mutableStateOf(customer!!.lastName) }
                    var address by remember(customer) { mutableStateOf(customer!!.address) }
                    // uses header-level isEditing state defined above

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isEditing = true },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.label_email, customer!!.email))
                            // Always show read-only personal info
                            Text(stringResource(R.string.label_name, customer!!.firstName, customer!!.lastName))
                            if (customer!!.address.isNotBlank()) {
                                Text(stringResource(R.string.label_address, customer!!.address))
                            }
                        }
                    }

                    // Chosen plan section
                    var showPlanDialog by remember { mutableStateOf(false) }
                    val customerId = customer!!.id
                    when {
                        loadingPlan -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(stringResource(R.string.loading_plan))
                            }
                        }

                        customerPlan == null -> {
                            Text(stringResource(R.string.no_plan_selected))
                            Button(onClick = {
                                showPlanDialog = true
                            }) { Text(stringResource(R.string.choose_plan)) }
                        }

                        else -> {
                            val plan = customerPlan!!.plan
                            val startDate = customerPlan!!.chosenDate
                            val days = plan.periodDays.amount
                            val endEpoch = startDate.toEpochDays() + (days - 1)
                            val endDate = kotlinx.datetime.LocalDate.fromEpochDays(endEpoch)
                            val totalPrice = plan.pricePerDay * days

                            val locale = Locale.forLanguageTag(AppLocaleManager.getCurrentLanguageTag(context))
                            val numberFormat = NumberFormat.getNumberInstance(locale)
                            val dateFormatter =
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

                            val kcalText = stringResource(
                                R.string.format_kcal,
                                numberFormat.format(plan.calories.amount),
                            )
                            val totalPriceText = numberFormat.format(totalPrice)
                            val startDateText = dateFormatter.format(startDate.toJavaLocalDate())
                            val endDateText = dateFormatter.format(endDate.toJavaLocalDate())

                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showPlanDialog = true },
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        stringResource(R.string.chosen_plan_title),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(stringResource(R.string.label_calories, kcalText))
                                    Text(pluralStringResource(R.plurals.days_quantity, days, days))
                                    Text(stringResource(R.string.label_total_price, totalPriceText))
                                    Text(stringResource(R.string.label_start_date, startDateText))
                                    Text(stringResource(R.string.label_end_date, endDateText))
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
                        Dialog(onDismissRequest = { isEditing = false }) {
                            Surface(shape = MaterialTheme.shapes.medium) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.edit_personal_info_title),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    OutlinedTextField(
                                        value = firstName,
                                        onValueChange = { firstName = it },
                                        label = { Text(stringResource(R.string.first_name)) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    OutlinedTextField(
                                        value = lastName,
                                        onValueChange = { lastName = it },
                                        label = { Text(stringResource(R.string.last_name)) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text(stringResource(R.string.address)) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    var saving by remember { mutableStateOf(false) }
                                    var saveError by remember { mutableStateOf<String?>(null) }
                                    if (saveError != null) {
                                        Text(
                                            stringResource(R.string.error_prefix, saveError!!),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            isEditing = false
                                        }) { Text(stringResource(R.string.cancel)) }
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
                                                        saveError = t.message ?: context.getString(R.string.save_failed)
                                                    } finally {
                                                        saving = false
                                                    }
                                                }
                                            },
                                            enabled = !saving,
                                        ) {
                                            Text(
                                                if (saving) {
                                                    stringResource(
                                                        R.string.saving,
                                                    )
                                                } else {
                                                    stringResource(R.string.save)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Language picker
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.language_section_title), style = MaterialTheme.typography.titleMedium)
                    val currentTag = remember { AppLocaleManager.getCurrentLanguageTag(context) }
                    var selectedTag by remember(currentTag) {
                        mutableStateOf(
                            when {
                                currentTag.startsWith("ru") -> "ru"
                                currentTag.startsWith("kk") -> "kk"
                                else -> "en"
                            },
                        )
                    }

                    val languages = listOf("English", "Русский", "Қазақ")
                    val initialIndex = when (selectedTag) {
                        "ru" -> 1
                        "kk" -> 2
                        else -> 0
                    }
                    val onSelect = { i: Int ->
                        selectedTag = when (i) {
                            1 -> "ru"
                            2 -> "kk"
                            else -> "en"
                        }
                        scope.launch {
                            AppLocaleManager.setLanguage(context, selectedTag)
                            (context as? Activity)?.recreate()
                        }
                        Unit
                    }
                    ToggleButtonsRow(languages, initialIndex, onSelect)

                    Spacer(Modifier.height(16.dp))
                    LogoutButton()
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
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
        runCatching { planApi.getAll() }
            .onSuccess { allPlans = it }
            .onFailure { error = it.message }
        loading = false
    }

    suspend fun loadCaloriesAndDays(preserveDay: Boolean) {
        error = null
        runCatching { planApi.getAvailableCalories(allowVariants) }
            .onSuccess { opts ->
                caloriesOptions = opts
                val targetCalories =
                    if (firstLoad.value) {
                        initialCalories
                    } else {
                        caloriesOptions.getOrNull(caloriesIndex.toInt())
                    }

                val idx = targetCalories?.let { opts.indexOf(it) }?.takeIf { it >= 0 } ?: 0
                caloriesIndex = idx.toFloat()

                val c = opts.getOrNull(idx)
                if (c != null) {
                    runCatching { planApi.getAvailableDays(allowVariants, c) }
                        .onSuccess { ds ->
                            daysOptions = ds
                            val targetDays =
                                if (firstLoad.value) {
                                    initialDays
                                } else if (preserveDay) {
                                    selectedDayIndex?.let { ds.getOrNull(it) }
                                } else {
                                    null
                                }
                            val dIdx = targetDays?.let { ds.indexOf(it) }?.takeIf { it >= 0 }
                                ?: if (ds.isNotEmpty()) 0 else null
                            selectedDayIndex = dIdx
                        }
                        .onFailure { err ->
                            error = err.message
                            daysOptions = emptyList()
                            selectedDayIndex = null
                        }
                } else {
                    daysOptions = emptyList()
                    selectedDayIndex = null
                }
            }
            .onFailure { error = it.message }
        firstLoad.value = false
    }

    LaunchedEffect(allowVariants) { loadCaloriesAndDays(preserveDay = false) }
    LaunchedEffect(caloriesIndex) {
        if (!firstLoad.value) {
            // Only reload days when the user actually changes calories
            val c = caloriesOptions.getOrNull(caloriesIndex.toInt())
            if (c != null) {
                runCatching { planApi.getAvailableDays(allowVariants, c) }
                    .onSuccess { ds ->
                        daysOptions = ds
                        selectedDayIndex = if (ds.isNotEmpty()) 0 else null
                    }
                    .onFailure { error = it.message }
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.choose_plan), style = MaterialTheme.typography.titleMedium)

                // Variants toggle
                ToggleButtonsRow(
                    options = listOf(stringResource(R.string.no_variants), stringResource(R.string.with_variants)),
                    initialSelectedIndex = if (allowVariants) 1 else 0,
                    onSelectedIndexChange = { i: Int -> allowVariants = i == 1 },
                )

                // Calories slider
                if (caloriesOptions.isNotEmpty()) {
                    val maxIndex = (caloriesOptions.size - 1).coerceAtLeast(0)
                    val steps = if (maxIndex >= 1) maxIndex - 1 else 0
                    val shownCalories = caloriesOptions.getOrNull(caloriesIndex.toInt())
                    val locale = Locale.forLanguageTag(AppLocaleManager.getCurrentLanguageTag(LocalContext.current))
                    val numberFormat = NumberFormat.getNumberInstance(locale)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val caloriesLabel = shownCalories?.let { numberFormat.format(it) } ?: "-"
                        Text(stringResource(R.string.label_calories, caloriesLabel))
                        Slider(
                            value = caloriesIndex,
                            onValueChange = { caloriesIndex = it.roundToInt().toFloat() },
                            valueRange = 0f..maxIndex.toFloat(),
                            steps = steps,
                        )
                    }
                } else {
                    Text(stringResource(R.string.no_plans_available))
                }

                // Selector of days
                if (daysOptions.isNotEmpty()) {
                    Text(stringResource(R.string.period_days))
                    ToggleButtonsRow(
                        options = daysOptions.map { it.toString() },
                        initialSelectedIndex = selectedDayIndex ?: 0,
                        onSelectedIndexChange = { i: Int -> selectedDayIndex = i },
                    )
                }

                run {
                    val locale = Locale.forLanguageTag(AppLocaleManager.getCurrentLanguageTag(LocalContext.current))
                    val numberFormat = NumberFormat.getNumberInstance(locale)
                    val pricePerDayText = matchingPlan?.pricePerDay?.let { numberFormat.format(it) } ?: "-"
                    val totalPriceText = totalPrice?.let { numberFormat.format(it) } ?: "-"
                    Text(stringResource(R.string.label_price_per_day, pricePerDayText))
                    Text(stringResource(R.string.label_total_price, totalPriceText))
                }

                if (error != null) {
                    Text(stringResource(R.string.error_prefix, error!!), color = MaterialTheme.colorScheme.error)
                }

                val saving = remember { mutableStateOf(false) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !saving.value,
                    ) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = {
                            if (matchingPlan != null && selectedDays != null) {
                                scope.launch {
                                    saving.value = true
                                    error = null
                                    try {
                                        val today = LocalDate.now().toKotlinLocalDate()
                                        val saved = customersApi.saveCustomerPlan(
                                            token,
                                            CustomerPlanCreateDto(planId = matchingPlan.id, chosenDate = today),
                                        )
                                        onSaved(saved)
                                    } catch (t: Throwable) {
                                        error = t.message ?: ctx.getString(R.string.save_failed)
                                    } finally {
                                        saving.value = false
                                    }
                                }
                            }
                        },
                        enabled = matchingPlan != null && !saving.value,
                    ) {
                        Text(if (saving.value) stringResource(R.string.saving) else stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
