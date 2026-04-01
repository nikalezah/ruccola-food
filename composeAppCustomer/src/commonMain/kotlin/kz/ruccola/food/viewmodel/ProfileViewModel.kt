package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

class ProfileViewModel : ViewModel() {
    private val customerApi = CustomerApi()
    private val authApi = AuthApi()
    private val planApi = PlanApi()

    val uiState: StateFlow<ProfileUiState>
        field = MutableStateFlow(ProfileUiState())

    fun loadProfile() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val customer = customerApi.get()
                uiState.update { it.copy(customer = customer) }
                loadCustomerPlan()
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadCustomerPlan() {
        uiState.update { it.copy(isLoadingPlan = true) }
        try {
            val customerPlan = customerApi.getCustomerPlan()
            uiState.update { it.copy(customerPlan = customerPlan) }
        } catch (e: Exception) {
            // Plan not found is ok
            uiState.update { it.copy(customerPlan = null) }
        } finally {
            uiState.update { it.copy(isLoadingPlan = false) }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                authApi.logout()
            } catch (e: Exception) {
                // Ignore logout error
            }
            onLoggedOut()
        }
    }

    fun updateCustomer(
        firstName: String,
        lastName: String,
        address: String,
    ) {
        viewModelScope.launch {
            uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val updated = customerApi.update(
                    CustomerUpdateDto(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        address = address.trim(),
                    ),
                )
                uiState.update { it.copy(customer = updated, isEditing = false) }
            } catch (e: Exception) {
                uiState.update { it.copy(saveError = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun setEditing(editing: Boolean) {
        uiState.update { it.copy(isEditing = editing, saveError = null) }
    }

    fun setShowPlanDialog(show: Boolean) {
        uiState.update { it.copy(showPlanDialog = show) }
        if (show) {
            loadPlansForDialog()
        }
    }

    private fun loadPlansForDialog() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoadingPlansForDialog = true, dialogError = null) }
            try {
                val allPlans = planApi.getAll()
                uiState.update { it.copy(allPlans = allPlans) }
                updateDialogOptions(preserveDay = true)
            } catch (e: Exception) {
                uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isLoadingPlansForDialog = false) }
            }
        }
    }

    fun setCaloriesIndex(index: Int) {
        if (uiState.value.caloriesIndex == index) return
        uiState.update { it.copy(caloriesIndex = index) }
        updateDialogOptions(preserveDay = false)
    }

    fun setSelectedDayIndex(index: Int?) {
        uiState.update { it.copy(selectedDayIndex = index) }
    }

    private fun updateDialogOptions(preserveDay: Boolean) {
        val state = uiState.value
        val allPlans = state.allPlans
        if (allPlans.isEmpty()) {
            uiState.update {
                it.copy(caloriesOptions = emptyList(), daysOptions = emptyList(), selectedDayIndex = null)
            }
            return
        }

        val caloriesOptions = allPlans.map { it.calories.amount }.distinct().sorted()
        uiState.update { it.copy(caloriesOptions = caloriesOptions) }

        val currentCalories = caloriesOptions.getOrNull(state.caloriesIndex)
        val targetCalories =
            currentCalories ?: state.customerPlan?.plan?.calories?.amount ?: caloriesOptions.first()
        val newCaloriesIndex = caloriesOptions.indexOf(targetCalories).coerceAtLeast(0)
        uiState.update { it.copy(caloriesIndex = newCaloriesIndex) }

        val selectedCalories = caloriesOptions[newCaloriesIndex]
        val daysOptions = allPlans.filter { it.calories.amount == selectedCalories }
            .map { it.periodDays.amount }.distinct().sorted()
        uiState.update { it.copy(daysOptions = daysOptions) }

        val oldDay = state.selectedDayIndex?.let { state.daysOptions.getOrNull(it) }
        val targetDay = if (preserveDay) {
            oldDay ?: state.customerPlan?.plan?.periodDays?.amount
        } else {
            null
        }
        val newDayIndex = targetDay?.let { daysOptions.indexOf(it) }?.takeIf { it >= 0 }
            ?: daysOptions.indices.firstOrNull()
        uiState.update { it.copy(selectedDayIndex = newDayIndex) }
    }

    fun savePlan() {
        val state = uiState.value
        val calories = state.caloriesOptions.getOrNull(state.caloriesIndex)
        val days = state.selectedDayIndex?.let { state.daysOptions.getOrNull(it) }
        val matchingPlan = state.allPlans.firstOrNull { p ->
            p.calories.amount == calories && p.periodDays.amount == days
        }

        if (matchingPlan != null) {
            viewModelScope.launch {
                uiState.update { it.copy(isSavingPlan = true, dialogError = null) }
                try {
                    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
                    val saved = customerApi.saveCustomerPlan(
                        CustomerPlanCreateDto(planId = matchingPlan.id, chosenDate = today),
                    )
                    uiState.update { it.copy(customerPlan = saved, showPlanDialog = false) }
                } catch (e: Exception) {
                    uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
                } finally {
                    uiState.update { it.copy(isSavingPlan = false) }
                }
            }
        }
    }
}

data class ProfileUiState(
    val customer: CustomerDto? = null,
    val customerPlan: CustomerPlanDetailsDto? = null,
    val isLoading: Boolean = false,
    val isLoadingPlan: Boolean = false,
    val error: String? = null,
    // Editing personal info
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    // Plan selection dialog
    val showPlanDialog: Boolean = false,
    val allPlans: List<PlanDto> = emptyList(),
    val isLoadingPlansForDialog: Boolean = false,
    val isSavingPlan: Boolean = false,
    val dialogError: String? = null,
    val caloriesOptions: List<Int> = emptyList(),
    val caloriesIndex: Int = 0,
    val daysOptions: List<Int> = emptyList(),
    val selectedDayIndex: Int? = null,
)
