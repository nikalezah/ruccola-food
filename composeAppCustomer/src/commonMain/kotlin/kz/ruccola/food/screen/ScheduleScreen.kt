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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import kz.ruccola.food.LocalStrings
import kz.ruccola.food.Strings
import kz.ruccola.food.dishImageUrl
import kz.ruccola.food.formatDate
import kz.ruccola.food.model.Meal
import kz.ruccola.food.ui.AsyncImage
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    token: String,
    viewModel: ScheduleViewModel = viewModel { ScheduleViewModel() },
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()

    // Local navigation to dish details
    var selectedDishId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(token) {
        viewModel.loadSchedule(token)
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
                title = { Text(strings.tabSchedule) },
            )
        },
    ) { padding ->
        val pullToRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadSchedule(token, isRefreshing = true) },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                uiState.error != null && uiState.week == null -> Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = strings.errorPrefix.replace("%s", uiState.error!!),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(onClick = { viewModel.loadSchedule(token) }) {
                        Text(strings.loading) // Or a retry string if available
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
                    Text(strings.noDishes)
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(uiState.week!!) { day ->
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        // Header: Day of week + DD.MM.YYYY
                        val header = remember(day.date, strings.locale) {
                            formatDate(day.date, strings.locale)
                        }
                        Text(
                            header,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Spacer(Modifier.height(6.dp))

                        if (day.dishes.isEmpty()) {
                            Text(
                                strings.noDishes,
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
                                            "${d.meal.time}\n${
                                                d.meal.toLocalizedString(strings).replaceFirst(' ', '\n')
                                            }",
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
}

private fun Meal.toLocalizedString(strings: Strings) =
    when (this) {
        Meal.BREAKFAST -> strings.mealBreakfast
        Meal.BRUNCH -> strings.mealBrunch
        Meal.LAUNCH -> strings.mealLunch
        Meal.AFTERNOON_SNACK -> strings.mealAfternoonSnack
        Meal.DINNER -> strings.mealDinner
    }
