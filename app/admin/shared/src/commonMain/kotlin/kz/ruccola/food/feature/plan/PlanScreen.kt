package kz.ruccola.food.feature.plan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.add
import food.composeappadmin.generated.resources.tab_plans
import kz.ruccola.food.api.PlanDto
import kz.ruccola.food.model.PlanCalories
import kz.ruccola.food.model.PlanDays
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ResponsiveContainer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    val vm: PlanViewModel = viewModel(factory = PlanViewModel.factory())
    val state by vm.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<PlanDto?>(null) }
    var prefillCalories by remember { mutableStateOf<PlanCalories?>(null) }
    var prefillDays by remember { mutableStateOf<PlanDays?>(null) }

    ResponsiveContainer(maxContentWidth = 900.dp) {
        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.tab_plans)) }) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingPlan = null
                        prefillCalories = null
                        prefillDays = null
                        showEditor = true
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.add))
                }
            },
        ) { padding ->
            PlanContent(
                state = state,
                onRefresh = { vm.loadAll() },
                onCellClick = { plan ->
                    prefillCalories = null
                    prefillDays = null
                    editingPlan = plan
                    showEditor = true
                },
                onEmptyCellClick = { cal, d ->
                    editingPlan = null
                    prefillCalories = cal
                    prefillDays = d
                    showEditor = true
                },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            showEditor = false
            vm.resetSaved()
        }
    }

    if (showEditor) {
        PlanEditorDialog(
            plan = editingPlan,
            initialCalories = prefillCalories,
            initialDays = prefillDays,
            onDismiss = {
                showEditor = false
                vm.clearError()
            },
            onSave = { cals, days, ppd ->
                if (editingPlan == null) {
                    vm.create(cals, days, ppd)
                } else {
                    vm.update(editingPlan!!.id, ppd)
                }
            },
            onDelete = { id ->
                vm.delete(id)
                showEditor = false
            },
            onClearError = { vm.clearError() },
            isSaving = state.isSaving,
            error = state.error,
        )
    }
}
