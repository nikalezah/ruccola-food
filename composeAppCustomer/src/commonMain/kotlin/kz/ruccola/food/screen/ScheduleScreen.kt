package kz.ruccola.food.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.loading
import food.composeappcustomer.generated.resources.meal_afternoon_snack
import food.composeappcustomer.generated.resources.meal_breakfast
import food.composeappcustomer.generated.resources.meal_brunch
import food.composeappcustomer.generated.resources.meal_dinner
import food.composeappcustomer.generated.resources.meal_lunch
import food.composeappcustomer.generated.resources.no_dishes
import food.composeappcustomer.generated.resources.tab_schedule
import kz.ruccola.food.dishImageUrl
import kz.ruccola.food.formatDate
import kz.ruccola.food.localization.LocalLocale
import kz.ruccola.food.model.Meal
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.ScheduleViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel { ScheduleViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLocale = LocalLocale.current

    // Local navigation to dish details
    var selectedDishId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSchedule()
    }

    // Dish details view
    selectedDishId?.let { dishId ->
        DishDetailsScreen(
            dishId = dishId,
            onBack = { selectedDishId = null },
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tab_schedule)) },
            )
        },
    ) {
        when {
            uiState.error != null && uiState.week == null -> Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(Res.string.error_prefix, uiState.error!!),
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = { viewModel.loadSchedule() }) {
                    Text(stringResource(Res.string.loading)) // Or a retry string if available
                }
            }

            uiState.isLoading && uiState.week == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            uiState.week.isNullOrEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(Res.string.no_dishes))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(uiState.week!!) { day ->
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    // Header: Day of week + DD.MM.YYYY
                    val header = remember(day.date, currentLocale) {
                        formatDate(day.date, currentLocale)
                    }
                    Text(
                        header,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Spacer(Modifier.height(6.dp))

                    if (day.dishes.isEmpty()) {
                        Text(
                            stringResource(Res.string.no_dishes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    } else {
                        day.dishes.forEach { d ->
                            val imageUrl = d.dish.images.firstOrNull()?.url
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
                                        "${d.meal.time}\n${d.meal.toLocalizedString().replaceFirst(' ', '\n')}",
                                        textAlign = TextAlign.End,
                                    )
                                },
                                modifier = Modifier.clickable(onClick = { selectedDishId = d.dish.id }),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
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
