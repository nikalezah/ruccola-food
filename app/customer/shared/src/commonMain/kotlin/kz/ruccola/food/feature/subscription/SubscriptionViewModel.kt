package kz.ruccola.food.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.CustomerPlanDetailsDto
import kz.ruccola.food.api.CustomerPrefsUpdateDto
import kz.ruccola.food.api.PlanApi
import kz.ruccola.food.api.PlanDto

class SubscriptionViewModel(
    private val customerApi: CustomerApi = CustomerApi(),
    private val planApi: PlanApi = PlanApi(),
) : ViewModel() {
    val uiState: StateFlow<SubscriptionUiState>
        field = MutableStateFlow(SubscriptionUiState())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch { loadPlanWithPrefs() }
    }

    private suspend fun loadPlanWithPrefs() {
        uiState.update { it.copy(isLoadingPlan = true) }
        try {
            val result = customerApi.getPlanWithPrefs()
            uiState.update {
                it.copy(
                    customerPlan = result.plan,
                    needsCutlery = result.prefs.needsCutlery,
                    weekendDelivery = result.prefs.weekendDelivery,
                    morningDelivery = result.prefs.morningDelivery,
                )
            }
        } catch (e: Exception) {
            uiState.update {
                it.copy(customerPlan = null, needsCutlery = false, weekendDelivery = false, morningDelivery = false)
            }
        } finally {
            uiState.update { it.copy(isLoadingPlan = false) }
        }
    }

    fun setShowPlanDialog(show: Boolean) {
        uiState.update { it.copy(showPlanDialog = show, caloriesIndex = 0, selectedDays = 1) }
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
                updateDialogOptions(preserveDays = true)
            } catch (e: Exception) {
                uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isLoadingPlansForDialog = false) }
            }
        }
    }

    fun setCaloriesIndex(index: Int) {
        if (uiState.value.caloriesIndex == index) return
        uiState.update { it.copy(caloriesIndex = index, dialogError = null) }
        updateDialogOptions(preserveDays = false)
    }

    fun setSelectedDays(days: Int) {
        uiState.update { it.copy(selectedDays = days.coerceIn(1, 30), dialogError = null) }
    }

    private fun updateDialogOptions(preserveDays: Boolean) {
        val state = uiState.value
        val allPlans = state.allPlans
        if (allPlans.isEmpty()) {
            uiState.update { it.copy(caloriesOptions = emptyList()) }
            return
        }

        val caloriesOptions = allPlans.map { it.calories.amount }.distinct().sorted()
        uiState.update { it.copy(caloriesOptions = caloriesOptions) }

        val planCalories = state.customerPlan?.calories
        val currentCalories = caloriesOptions.getOrNull(state.caloriesIndex)
        val targetCalories =
            if (preserveDays && planCalories != null && planCalories in caloriesOptions) {
                planCalories
            } else {
                currentCalories ?: caloriesOptions.first()
            }
        val newCaloriesIndex = caloriesOptions.indexOf(targetCalories).coerceAtLeast(0)
        uiState.update { it.copy(caloriesIndex = newCaloriesIndex) }

        if (preserveDays) {
            val planDays = state.customerPlan?.days
            if (planDays != null) {
                uiState.update { it.copy(selectedDays = planDays.coerceIn(1, 30)) }
            }
        }
    }

    fun effectivePricePerDay(): Int? {
        val state = uiState.value
        val selectedCalories = state.caloriesOptions.getOrNull(state.caloriesIndex) ?: return null
        return state.allPlans
            .filter { it.calories.amount == selectedCalories && it.periodDays.amount <= state.selectedDays }
            .maxByOrNull { it.periodDays.amount }
            ?.pricePerDay
    }

    fun thresholdPlan(): PlanDto? {
        val state = uiState.value
        val selectedCalories = state.caloriesOptions.getOrNull(state.caloriesIndex) ?: return null
        return state.allPlans
            .filter { it.calories.amount == selectedCalories && it.periodDays.amount <= state.selectedDays }
            .maxByOrNull { it.periodDays.amount }
    }

    fun savePlan() {
        val state = uiState.value
        val plan = thresholdPlan() ?: return

        viewModelScope.launch {
            uiState.update { it.copy(isSavingPlan = true, dialogError = null) }
            try {
                val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
                val saved =
                    customerApi.saveCustomerPlan(
                        CustomerPlanCreateDto(planId = plan.id, days = state.selectedDays, chosenDate = today)
                    )
                uiState.update { it.copy(customerPlan = saved, showPlanDialog = false) }
                loadPlanWithPrefs()
            } catch (e: Exception) {
                uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
            } finally {
                uiState.update { it.copy(isSavingPlan = false) }
            }
        }
    }

    fun updateDeliveryPrefs(
        needsCutlery: Boolean? = null,
        weekendDelivery: Boolean? = null,
        morningDelivery: Boolean? = null,
    ) {
        viewModelScope.launch {
            try {
                val result =
                    customerApi.saveDeliveryPrefs(
                        CustomerPrefsUpdateDto(
                            needsCutlery = needsCutlery,
                            weekendDelivery = weekendDelivery,
                            morningDelivery = morningDelivery,
                        )
                    )
                uiState.update {
                    it.copy(
                        needsCutlery = result.needsCutlery,
                        weekendDelivery = result.weekendDelivery,
                        morningDelivery = result.morningDelivery,
                    )
                }
            } catch (e: Exception) {
                // todo: show toast with an error message and revert control values if needed
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory { initializer { SubscriptionViewModel() } }
    }
}

data class SubscriptionUiState(
    val customerPlan: CustomerPlanDetailsDto? = null,
    val isLoadingPlan: Boolean = false,
    val showPlanDialog: Boolean = false,
    val allPlans: List<PlanDto> = emptyList(),
    val isLoadingPlansForDialog: Boolean = false,
    val isSavingPlan: Boolean = false,
    val dialogError: String? = null,
    val caloriesOptions: List<Int> = emptyList(),
    val caloriesIndex: Int = 0,
    val selectedDays: Int = 1,
    val needsCutlery: Boolean = false,
    val weekendDelivery: Boolean = false,
    val morningDelivery: Boolean = false,
)
