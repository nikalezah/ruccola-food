package kz.ruccola.food.admin.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.Strings
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.MealPlanDayApi
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.model.Meal
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.FabMenu
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeToRemove
import kz.ruccola.food.ui.dishImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanDayEditorScreen(
    initialItem: MealPlanDayDto?,
    nextSerial: Int,
    onClose: () -> Unit,
) {
    val api = remember { MealPlanDayApi() }
    val dishApi = remember { DishApi() }
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Local staged state
    val localDishIdToMeal = remember { mutableStateMapOf<Int, Meal>() }
    val localDishes = remember { mutableStateListOf<DishWithMealDto>() }
    var initialized by remember { mutableStateOf(false) }

    // Initialize from a server for editing or empty for creating
    LaunchedEffect(initialItem?.id) {
        initialized = false
        localDishIdToMeal.clear()
        localDishes.clear()
        if (initialItem != null) {
            busy = true
            try {
                val dishes = api.getDishes(initialItem.id)
                dishes.forEach { d ->
                    localDishIdToMeal[d.dish.id] = d.meal
                    localDishes.add(d)
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                busy = false
                initialized = true
            }
        } else {
            initialized = true
        }
    }

    fun save() {
        scope.launch {
            busy = true
            error = null
            try {
                api.save(MealPlanDaySaveDto(id = initialItem?.id, dishIdToMeal = localDishIdToMeal.toMap()))
                onClose()
            } catch (e: Exception) {
                error = e.message
            } finally {
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val serial = initialItem?.serial ?: nextSerial
                    Text(if (initialItem == null) "Новый день №$serial" else "Изменить день №$serial")
                },
                navigationIcon = {
                    IconButton(onClick = { if (!busy) onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledIconButton(onClick = { save() }, enabled = initialized && !busy) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
        floatingActionButton = {
            if (initialized) {
                var showPickerForMeal by remember { mutableStateOf<Meal?>(null) }
                FabMenu(
                    Meal.entries.map { meal ->
                        Triple(null, meal.toLocalizedString()) { showPickerForMeal = meal }
                    },
                )

                if (showPickerForMeal != null) {
                    var dishes by remember { mutableStateOf<List<DishDto>>(emptyList()) }
                    var dishesLoading by remember { mutableStateOf(true) }
                    var dishesError by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        try {
                            dishes = dishApi.getAllDishes().filter { !it.archived }
                        } catch (e: Exception) {
                            dishesError = e.message
                        } finally {
                            dishesLoading = false
                        }
                    }

                    AlertDialog(
                        onDismissRequest = { showPickerForMeal = null },
                        title = { Text("Выберите блюдо для ${showPickerForMeal!!.toLocalizedString()}") },
                        text = {
                            Box(modifier = Modifier.sizeIn(minWidth = 300.dp, maxWidth = 500.dp, maxHeight = 400.dp)) {
                                if (dishesLoading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                } else if (dishesError != null) {
                                    Text(dishesError!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(dishes) { dish ->
                                            OutlinedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = {
                                                    val meal = showPickerForMeal ?: return@OutlinedCard
                                                    val idx = localDishes.indexOfFirst { it.dish.id == dish.id }
                                                    if (idx >= 0) {
                                                        localDishes[idx] = localDishes[idx].copy(meal = meal)
                                                    } else {
                                                        localDishes.add(DishWithMealDto(dish = dish, meal = meal))
                                                    }
                                                    localDishIdToMeal[dish.id] = meal
                                                    showPickerForMeal = null
                                                },
                                            ) {
                                                Row(
                                                    Modifier.fillMaxWidth().padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    val img = dish.images.firstOrNull()
                                                    if (img != null) {
                                                        AsyncImage(
                                                            model = dishImageUrl(img.url),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(40.dp),
                                                        )
                                                        Spacer(Modifier.width(12.dp))
                                                    }
                                                    Text(dish.name, modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showPickerForMeal = null }) { Text(Strings.cancel) }
                        },
                    )
                }
            }
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
        ) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                Spacer(Modifier.height(8.dp))
            }

            if (busy && !initialized) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            } else {
                localDishes.forEach { d ->
                    val onDelete: () -> Unit = {
                        localDishIdToMeal.remove(d.dish.id)
                        localDishes.removeAll { it.dish.id == d.dish.id }
                    }
                    SwipeToRemove(Icons.Default.Delete, Strings.delete, onDelete) {
                        ListItem(
                            leadingContent = {
                                val imageUrl = d.dish.images.firstOrNull()?.url
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
                                    "${d.meal.time}\n${d.meal.toLocalizedString().replaceFirst(' ', '\n')}",
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

fun Meal.toLocalizedString() =
    when (this) {
        Meal.BREAKFAST -> Strings.mealBreakfast
        Meal.BRUNCH -> Strings.mealBrunch
        Meal.LAUNCH -> Strings.mealLunch
        Meal.AFTERNOON_SNACK -> Strings.mealAfternoonSnack
        Meal.DINNER -> Strings.mealDinner
    }
