package kz.ruccola.food.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kz.ruccola.food.api.CustomerApi
import kz.ruccola.food.api.WeeklyPlanDayDto
import kz.ruccola.food.localization.AppLocaleManager
import kz.ruccola.food.localization.toLocalizedString
import kz.ruccola.food.ui.SingleLineText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AndroidCustomerWeekScreen(token: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var week by remember { mutableStateOf<List<WeeklyPlanDayDto>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Local navigation to dish details
    var selectedDishId by remember { mutableStateOf<Int?>(null) }

    // Expand/collapse state per day+meal
    val expandedState = remember { mutableStateMapOf<String, Boolean>() } // key: "$date|${meal.name}"

    LaunchedEffect(Unit) {
        scope.launch {
            runCatching {
                CustomerApi().getWeek(token)
            }.onSuccess { loadedWeek ->
                week = loadedWeek
            }.onFailure { e ->
                error = e.message ?: e.toString()
            }
        }
    }

    // Dish details view
    selectedDishId?.let { dishId ->
        AndroidDishDetailsScreen(
            dishId = dishId,
            onBack = { selectedDishId = null },
        )
        return
    }

    when {
        error != null -> Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
        }

        week == null -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        }

        else -> LazyColumn(contentPadding = PaddingValues(8.dp)) {
            items(week!!) { day ->
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                // Header: Day of week + DD.MM.YYYY
                val header =
                    remember(day.date) { formatDate(day.date.toString(), AppLocaleManager.getCurrentLocale(context)) }
                Text(header, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(6.dp))

                if (day.dishes.isEmpty()) {
                    Text(
                        "No dishes",
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
                                    "${d.meal.time}\n${d.meal.toLocalizedString(context).replaceFirst(' ', '\n')}",
                                    textAlign = TextAlign.End,
                                )
                            },
                            modifier = Modifier.clickable(onClick = { selectedDishId = d.dish.id }),
                        )
                    }

                    /* Expandable list of dishes grouped by meal
                    val grouped = remember(day.dishes) { day.dishes.groupBy({ it.meal }, { it.dish }) }
                    Meal.entries.forEach { meal ->
                        val dishes = grouped[meal].orEmpty()
                        if (dishes.isNotEmpty()) {
                            val key = day.date + "|" + meal.name
                            val expanded = expandedState.getOrPut(key) { true }

                            ListItem(
                                headlineContent = { Text(meal.label) },
                                trailingContent = {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedState[key] = !expanded }
                            )
                            if (expanded) {
                                dishes.forEach { dish ->
                                    val imageUrl = remember(dish) { dish.images.firstOrNull()?.url }
                                    ListItem(
                                        leadingContent = {
                                            if (imageUrl != null) {
                                                AsyncImage(
                                                    model = dishImageUrl(imageUrl),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(56.dp)
                                                )
                                            }
                                        },
                                        headlineContent = { Text(dish.name) },
                                        modifier = Modifier.clickable(onClick = { selectedDishId = dish.id })
                                    )
                                    HorizontalDivider()
                                }
                            }
                            HorizontalDivider(thickness = 1.dp)
                        }
                    }
                     */
                }
            }
        }
    }
}

fun formatDate(
    isoDate: String,
    locale: Locale,
): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", locale)
    return LocalDate.parse(isoDate).format(formatter).replaceFirstChar { it.titlecase() }
}
