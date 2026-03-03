package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val customer = customerApi.get(token)
                _uiState.update { it.copy(customer = customer) }
                loadCustomerPlan(token)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadCustomerPlan(token: String) {
        _uiState.update { it.copy(isLoadingPlan = true) }
        try {
            val customerPlan = customerApi.getCustomerPlan(token)
            _uiState.update { it.copy(customerPlan = customerPlan) }
        } catch (e: Exception) {
            // Plan not found is ok
            _uiState.update { it.copy(customerPlan = null) }
        } finally {
            _uiState.update { it.copy(isLoadingPlan = false) }
        }
    }

    fun logout(
        token: String,
        onLoggedOut: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                authApi.logout(token)
            } catch (e: Exception) {
                // Ignore logout error
            }
            onLoggedOut()
        }
    }

    fun updateCustomer(
        token: String,
        firstName: String,
        lastName: String,
        address: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val updated = customerApi.update(
                    token,
                    CustomerUpdateDto(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        address = address.trim(),
                    ),
                )
                _uiState.update { it.copy(customer = updated, isEditing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = e.message ?: e.toString()) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun setEditing(editing: Boolean) {
        _uiState.update { it.copy(isEditing = editing, saveError = null) }
    }

    fun setShowPlanDialog(show: Boolean) {
        _uiState.update { it.copy(showPlanDialog = show) }
        if (show) {
            loadPlansForDialog()
        }
    }

    private fun loadPlansForDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlansForDialog = true, dialogError = null) }
            try {
                val allPlans = planApi.getAll()
                _uiState.update { it.copy(allPlans = allPlans) }
                updateDialogOptions(preserveDay = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
            } finally {
                _uiState.update { it.copy(isLoadingPlansForDialog = false) }
            }
        }
    }

    fun setAllowVariants(allow: Boolean) {
        if (_uiState.value.allowVariants == allow) return
        _uiState.update { it.copy(allowVariants = allow) }
        viewModelScope.launch {
            updateDialogOptions(preserveDay = true)
        }
    }

    fun setCaloriesIndex(index: Int) {
        if (_uiState.value.caloriesIndex == index) return
        _uiState.update { it.copy(caloriesIndex = index) }
        viewModelScope.launch {
            updateDialogOptions(preserveDay = false)
        }
    }

    fun setSelectedDayIndex(index: Int?) {
        _uiState.update { it.copy(selectedDayIndex = index) }
    }

    private suspend fun updateDialogOptions(preserveDay: Boolean) {
        val state = _uiState.value
        try {
            val caloriesOptions = planApi.getAvailableCalories(state.allowVariants)
            _uiState.update { it.copy(caloriesOptions = caloriesOptions) }

            // Try to keep current calories if possible, or use initial if it's first load
            val currentCalories = caloriesOptions.getOrNull(state.caloriesIndex)
            val targetCalories =
                currentCalories ?: state.customerPlan?.plan?.calories?.amount ?: caloriesOptions.firstOrNull()
            val newCaloriesIndex = targetCalories?.let { caloriesOptions.indexOf(it) }?.takeIf { it >= 0 } ?: 0
            _uiState.update { it.copy(caloriesIndex = newCaloriesIndex) }

            val selectedCalories = caloriesOptions.getOrNull(newCaloriesIndex)
            if (selectedCalories != null) {
                val daysOptions = planApi.getAvailableDays(state.allowVariants, selectedCalories)
                _uiState.update { it.copy(daysOptions = daysOptions) }

                val oldDay = state.selectedDayIndex?.let { state.daysOptions.getOrNull(it) }
                val targetDay = if (preserveDay) {
                    oldDay ?: state.customerPlan?.plan?.periodDays?.amount
                } else {
                    null
                }
                val newDayIndex = targetDay?.let { daysOptions.indexOf(it) }?.takeIf { it >= 0 }
                    ?: if (daysOptions.isNotEmpty()) 0 else null
                _uiState.update { it.copy(selectedDayIndex = newDayIndex) }
            } else {
                _uiState.update { it.copy(daysOptions = emptyList(), selectedDayIndex = null) }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
        }
    }

    fun savePlan(token: String) {
        val state = _uiState.value
        val calories = state.caloriesOptions.getOrNull(state.caloriesIndex)
        val days = state.selectedDayIndex?.let { state.daysOptions.getOrNull(it) }
        val matchingPlan = state.allPlans.firstOrNull { p ->
            p.allowVariantChoice == state.allowVariants &&
                p.calories.amount == calories &&
                p.periodDays.amount == days
        }

        if (matchingPlan != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSavingPlan = true, dialogError = null) }
                try {
                    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
                    val saved = customerApi.saveCustomerPlan(
                        token,
                        CustomerPlanCreateDto(planId = matchingPlan.id, chosenDate = today),
                    )
                    _uiState.update { it.copy(customerPlan = saved, showPlanDialog = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(dialogError = e.message ?: e.toString()) }
                } finally {
                    _uiState.update { it.copy(isSavingPlan = false) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
    val allowVariants: Boolean = false,
    val caloriesOptions: List<Int> = emptyList(),
    val caloriesIndex: Int = 0,
    val daysOptions: List<Int> = emptyList(),
    val selectedDayIndex: Int? = null,
)
