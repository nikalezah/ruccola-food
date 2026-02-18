package kz.ruccola.food.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.localization.toLocalizedString
import kz.ruccola.food.model.Meal
import kz.ruccola.food.ui.ApplyIconButton
import kz.ruccola.food.ui.FabMenu
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeBackground
import kz.ruccola.food.ui.dishImageUrl
import kz.ruccola.food.viewmodel.DishViewModel
import kz.ruccola.food.viewmodel.MealPlanDayViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidMealPlanDayEditorScreen(
    initialItem: MealPlanDayDto?,
    nextSerial: Int,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val vm: MealPlanDayViewModel = viewModel()
    val dishesVm: DishViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val dishesState by dishesVm.uiState.collectAsState()

    // Ensure a system back gesture navigates back instead of exiting
    BackHandler(enabled = true) { onClose() }

    // Show toast for errors
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    // Local staged state: user edits are stored here and only sent on Save
    val localDishIdToMeal = remember { mutableStateMapOf<Int, Meal>() }
    val localDishes = remember { mutableStateListOf<DishWithMealDto>() }
    var initialized by remember { mutableStateOf(false) }

    // Initialize from a server for editing or empty for creating
    LaunchedEffect(initialItem?.id) {
        initialized = false
        localDishIdToMeal.clear()
        localDishes.clear()
        if (initialItem != null) {
            vm.getDishes(initialItem.id)
        } else {
            initialized = true
        }
    }

    // When dishes are loaded for editing, copy to the local staged state once (only for that item)
    LaunchedEffect(state.selectedDishes) {
        if (!initialized && initialItem != null && state.selectedDishesForId == initialItem.id) {
            localDishIdToMeal.clear()
            localDishes.clear()
            state.selectedDishes.forEach { d ->
                localDishIdToMeal[d.dish.id] = d.meal
                localDishes.add(d)
            }
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val serial = initialItem?.serial ?: nextSerial
                    Text(if (initialItem == null) "Новый день №$serial" else "Изменить день №$serial")
                },
                subtitle = { Text("Блюда приёмов пищи") },
                navigationIcon = {
                    IconButton(onClick = { onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ApplyIconButton(
                        onClick = {
                            vm.save(initialItem?.id, localDishIdToMeal.toMap())
                            onClose()
                        },
                        enabled = initialized,
                    )
                },
            )
        },
        floatingActionButton = {
            if (initialized) {
                var showPickerForMeal by remember { mutableStateOf<Meal?>(null) }
                FabMenu(
                    Meal.entries.map {
                        Triple(null, it.toLocalizedString(context)) { showPickerForMeal = it }
                    },
                )
                if (showPickerForMeal != null) {
                    LaunchedEffect(showPickerForMeal) {
                        if (showPickerForMeal != null) {
                            dishesVm.loadDishes()
                        }
                    }
                    AlertDialog(
                        onDismissRequest = { showPickerForMeal = null },
                        title = { Text("Pick a Dish for ${showPickerForMeal!!.toLocalizedString(context)}") },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (dishesState.isLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 400.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 400.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        items(dishesState.dishes) { dish ->
                                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    Modifier.fillMaxWidth().padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Text(
                                                        dish.name,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                    TextButton(
                                                        onClick = {
                                                            val meal = showPickerForMeal ?: return@TextButton
                                                            val idx = localDishes.indexOfFirst { it.dish.id == dish.id }
                                                            if (idx >= 0) {
                                                                localDishes[idx] = localDishes[idx].copy(meal = meal)
                                                            } else {
                                                                localDishes.add(
                                                                    DishWithMealDto(
                                                                        dish = dish,
                                                                        meal = meal,
                                                                    ),
                                                                )
                                                            }
                                                            localDishIdToMeal[dish.id] = meal
                                                            showPickerForMeal = null
                                                        },
                                                    ) { Text("Add") }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showPickerForMeal = null }) { Text("Close") }
                        },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ) {
            localDishes.forEach { d ->
                val imageUrl = d.dish.images.firstOrNull()?.url
                val dismissState = rememberSwipeToDismissBoxState()

                LaunchedEffect(dismissState.currentValue) {
                    if (
                        dismissState.currentValue == SwipeToDismissBoxValue.EndToStart ||
                        dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd
                    ) {
                        localDishIdToMeal.remove(d.dish.id)
                        localDishes.removeAll { it.dish.id == d.dish.id }
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.matchParentSize().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SwipeBackground(Icons.Default.Delete, "Delete")
                                SwipeBackground(Icons.Default.Delete, "Delete")
                            }
                        }
                    },
                ) {
                    ListItem(
                        leadingContent = {
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = dishImageUrl(imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
                                )
                            }
                        },
                        headlineContent = { SingleLineText(d.dish.name) },
                        supportingContent = { SingleLineText(d.dish.description) },
                        trailingContent = {
                            Text(
                                "${d.meal.time}\n${d.meal.toLocalizedString(context).replaceFirst(' ', '\n')}",
                                textAlign = TextAlign.End,
                            )
                        },
                    )
                }
            }
        }
    }
}
