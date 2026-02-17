package kz.ruccola.food.customer.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.WeeklyPlanDayDto
import kz.ruccola.food.customer.LocalStrings
import kz.ruccola.food.customer.Strings
import kz.ruccola.food.model.Meal
import kz.ruccola.food.web.common.dishImageUrl
import kz.ruccola.food.web.common.ui.AsyncImage
import kz.ruccola.food.web.common.ui.SingleLineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(token: String) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    var week by remember { mutableStateOf<List<WeeklyPlanDayDto>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Local navigation to dish details
    var selectedDishId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(token) {
        scope.launch {
            try {
                week = CustomerApi().getWeek(token)
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            }
        }
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
        when {
            error != null -> Column(
                modifier = Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            }

            week == null -> Box(
                modifier = Modifier.fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.padding(16.dp))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(week!!) { day ->
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    // Header: Day of week + DD.MM.YYYY
                    val header = remember(day.date, strings.locale) {
                        formatDate(day.date.toString(), strings.locale)
                    }
                    Text(
                        header,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(Modifier.height(6.dp))

                    if (day.dishes.isEmpty()) {
                        Text(
                            strings.noDishes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        "${d.meal.time}\n${d.meal.toLocalizedString(strings).replaceFirst(' ', '\n')}",
                                        textAlign = TextAlign.End,
                                    )
                                },
                                modifier = Modifier.clickable(onClick = { selectedDishId = d.dish.id }),
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Meal.toLocalizedString(strings: Strings) =
    when (this) {
        Meal.BREAKFAST -> strings.mealBreakfast
        Meal.BRUNCH -> strings.mealBrunch
        Meal.LAUNCH -> strings.mealLunch
        Meal.AFTERNOON_SNACK -> strings.mealAfternoonSnack
        Meal.DINNER -> strings.mealDinner
    }

fun formatDate(
    isoDate: String,
    locale: String,
): String {
    return isoDate
    /* todo: fix and use
        try {
            val date = LocalDate.parse(isoDate)
            val jsDate = kotlin.js.Date(date.year, date.month.number - 1, date.day)
            val options = kotlin.js.json(
                "weekday" to "long",
                "year" to "numeric",
                "month" to "2-digit",
                "day" to "2-digit"
            ).unsafeCast<kotlin.js.Date.LocaleOptions>()

            return jsDate.toLocaleDateString(locale, options).replaceFirstChar { it.titlecase() }
        } catch (e: Exception) {
            return isoDate
        }
     */
}
