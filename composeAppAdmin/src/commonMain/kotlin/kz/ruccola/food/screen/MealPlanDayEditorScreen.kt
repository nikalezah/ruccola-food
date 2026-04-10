package kz.ruccola.food.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.cancel
import food.composeappadmin.generated.resources.close
import food.composeappadmin.generated.resources.delete
import food.composeappadmin.generated.resources.edit_day
import food.composeappadmin.generated.resources.meal_afternoon_snack
import food.composeappadmin.generated.resources.meal_breakfast
import food.composeappadmin.generated.resources.meal_brunch
import food.composeappadmin.generated.resources.meal_dinner
import food.composeappadmin.generated.resources.meal_lunch
import food.composeappadmin.generated.resources.mpd_subtitle
import food.composeappadmin.generated.resources.new_day
import food.composeappadmin.generated.resources.pick_dish_for
import food.composeappadmin.generated.resources.save
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.api.DishWithTranslationsDto
import kz.ruccola.food.api.MealPlanDayDto
import kz.ruccola.food.dishImageUrl
import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.Meal
import kz.ruccola.food.ui.ApplyIconButton
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.FabMenu
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.ui.SwipeToRemove
import kz.ruccola.food.viewmodel.DishViewModel
import kz.ruccola.food.viewmodel.MealPlanDayViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MealPlanDayEditorScreen(
    mealPlanDay: MealPlanDayDto?,
    nextSerial: Int,
    onClose: () -> Unit,
) {
    val vm: MealPlanDayViewModel = viewModel(factory = MealPlanDayViewModel.Factory)
    val dishVm: DishViewModel = viewModel(factory = DishViewModel.Factory)
    val state by vm.uiState.collectAsState()
    val pagedDishes = dishVm.dishes.collectAsLazyPagingItems()

    // Local staged state
    val localDishIdToMeal = remember { mutableStateMapOf<Int, Meal>() }
    val localDishes = remember { mutableStateListOf<DishWithMealDto>() }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(mealPlanDay?.id) {
        localDishIdToMeal.clear()
        localDishes.clear()
        mealPlanDay?.dishes?.forEach { d ->
            localDishIdToMeal[d.dish.id] = d.meal
            localDishes.add(d)
        }
        initialized = true
    }

    val initialDishIdToMeal = remember(mealPlanDay?.id) {
        mealPlanDay?.dishes?.associate { it.dish.id to it.meal } ?: emptyMap()
    }
    val initialDishIds = remember(mealPlanDay?.id) {
        mealPlanDay?.dishes?.map { it.dish.id }?.toSet() ?: emptySet()
    }
    val hasChanges = localDishIdToMeal.toMap() != initialDishIdToMeal ||
        localDishes.map { it.dish.id }.toSet() != initialDishIds

    fun save() {
        vm.save(mealPlanDay?.id, localDishIdToMeal.toMap())
        onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val serial = mealPlanDay?.serial ?: nextSerial
                    val titleRes = if (mealPlanDay == null) Res.string.new_day else Res.string.edit_day
                    Text(stringResource(titleRes, serial.toString()))
                },
                subtitle = { Text(stringResource(Res.string.mpd_subtitle)) },
                navigationIcon = {
                    IconButton(onClick = { if (!state.isSaving) onClose() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(Res.string.close))
                    }
                },
                actions = {
                    ApplyIconButton(
                        onClick = { save() },
                        enabled = initialized && !state.isSaving && hasChanges,
                        contentDescription = stringResource(Res.string.save),
                    )
                },
            )
        },
        floatingActionButton = {
            if (initialized) {
                var showPickerForMeal by remember { mutableStateOf<Meal?>(null) }
                FabMenu(
                    Meal.entries.map { meal ->
                        val label = meal.toLocalizedString()
                        Triple(null, label) { showPickerForMeal = meal }
                    },
                )

                if (showPickerForMeal != null) {
                    val mealToPick = showPickerForMeal!!

                    AlertDialog(
                        onDismissRequest = { showPickerForMeal = null },
                        title = { Text(stringResource(Res.string.pick_dish_for, mealToPick.toLocalizedString())) },
                        text = {
                            Box(modifier = Modifier.sizeIn(minWidth = 300.dp, maxWidth = 500.dp, maxHeight = 400.dp)) {
                                if (pagedDishes.loadState.refresh is LoadState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(
                                            count = pagedDishes.itemCount,
                                            key = pagedDishes.itemKey { it.id },
                                        ) { index ->
                                            val dish = pagedDishes[index] ?: return@items
                                            OutlinedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = {
                                                    val meal = showPickerForMeal ?: return@OutlinedCard
                                                    val idx = localDishes.indexOfFirst { it.dish.id == dish.id }
                                                    if (idx >= 0) {
                                                        localDishes[idx] = localDishes[idx].copy(meal = meal)
                                                    } else {
                                                        localDishes.add(
                                                            DishWithMealDto(dish = dish.toDishDto(), meal = meal),
                                                        )
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
                                                    Text(
                                                        dish.translations[Language.adminDefault]!!.name,
                                                        modifier = Modifier.weight(1f),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showPickerForMeal = null }) {
                                Text(stringResource(Res.string.cancel))
                            }
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
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                Spacer(Modifier.height(8.dp))
            }

            if (state.isSaving || (state.isLoadingDishes && !initialized)) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            } else {
                localDishes.forEach { d ->
                    key(d.dish.id) {
                        val onDelete: () -> Unit = {
                            localDishIdToMeal.remove(d.dish.id)
                            localDishes.removeAll { it.dish.id == d.dish.id }
                        }
                        SwipeToRemove(Icons.Filled.Delete, stringResource(Res.string.delete), onDelete) {
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
                                    val timeText = d.meal.time.let {
                                        "${it.hour.toString().padStart(2, '0')}:${
                                            it.minute.toString().padStart(2, '0')
                                        }"
                                    }
                                    Text(
                                        "${timeText}\n${d.meal.toLocalizedString().replaceFirst(' ', '\n')}",
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
}

@Composable
private fun Meal.toLocalizedString() =
    when (this) {
        Meal.BREAKFAST -> stringResource(Res.string.meal_breakfast)
        Meal.BRUNCH -> stringResource(Res.string.meal_brunch)
        Meal.LAUNCH -> stringResource(Res.string.meal_lunch)
        Meal.AFTERNOON_SNACK -> stringResource(Res.string.meal_afternoon_snack)
        Meal.DINNER -> stringResource(Res.string.meal_dinner)
    }

// todo: remove
private fun DishWithTranslationsDto.toDishDto(language: Language = Language.adminDefault) =
    DishDto(
        id,
        translations[language]!!.name,
        translations[language]!!.description,
        archived,
        createdAt,
        updatedAt,
        images,
    )
