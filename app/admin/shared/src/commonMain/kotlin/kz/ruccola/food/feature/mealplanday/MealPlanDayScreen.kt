package kz.ruccola.food.feature.mealplanday

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.screen_history_title
import food.composeappadmin.generated.resources.tab_schedule
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ResponsiveContainer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MealPlanDayScreen(onHistoryClick: () -> Unit) {
    val vm: MealPlanDayViewModel = viewModel(factory = MealPlanDayViewModel.factory())
    val state by vm.uiState.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingDay by remember { mutableStateOf<MealPlanDayDto?>(null) }
    var nextSerial by remember { mutableIntStateOf(1) }
    var reorderMode by remember { mutableStateOf(false) }

    val workingList = remember { mutableStateListOf<MealPlanDayDto>() }
    LaunchedEffect(state.items) {
        workingList.clear()
        workingList.addAll(state.items)
    }

    fun isOrderChanged(): Boolean {
        val originalIds = state.items.map { it.id }
        val workingIds = workingList.map { it.id }
        return originalIds != workingIds
    }

    if (showEditor) {
        MealPlanDayEditorScreen(
            mealPlanDay = editingDay,
            nextSerial = nextSerial,
            onClose = {
                showEditor = false
                editingDay = null
                vm.loadAll()
            },
        )
    } else {
        ResponsiveContainer(maxContentWidth = 720.dp) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(Res.string.tab_schedule)) },
                        navigationIcon = {
                            IconButton(onClick = onHistoryClick) {
                                Icon(
                                    Icons.Filled.History,
                                    contentDescription = stringResource(Res.string.screen_history_title),
                                )
                            }
                        },
                        actions = {
                            FilledIconToggleButton(
                                checked = reorderMode,
                                onCheckedChange = { checked ->
                                    if (reorderMode) {
                                        if (isOrderChanged()) {
                                            vm.reorder(workingList.map { it.id })
                                        }
                                    }
                                    reorderMode = checked
                                },
                                modifier =
                                    Modifier.size(
                                        IconButtonDefaults.smallContainerSize(
                                            widthOption = IconButtonDefaults.IconButtonWidthOption.Wide
                                        )
                                    ),
                            ) {
                                if (reorderMode) {
                                    Icon(Icons.Filled.Check, contentDescription = "Done")
                                } else {
                                    Icon(Icons.Filled.SwapVert, contentDescription = "Reorder")
                                }
                            }
                        },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            nextSerial = (state.items.maxOfOrNull { it.serial } ?: 0) + 1
                            editingDay = null
                            showEditor = true
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                },
            ) { padding ->
                MealPlanDayContent(
                    state = state,
                    workingList = workingList,
                    reorderMode = reorderMode,
                    onRefresh = { vm.loadAll() },
                    onItemClick = { day ->
                        editingDay = day
                        showEditor = true
                    },
                    onDelete = { vm.delete(it) },
                    onMakeCurrent = { vm.setCurrent(it) },
                    onMoveUp = { day ->
                        val idx = workingList.indexOf(day)
                        if (idx > 0) {
                            workingList.removeAt(idx)
                            workingList.add(idx - 1, day)
                        }
                    },
                    onMoveDown = { day ->
                        val idx = workingList.indexOf(day)
                        if (idx < workingList.size - 1) {
                            workingList.removeAt(idx)
                            workingList.add(idx + 1, day)
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
        }
    }
}
