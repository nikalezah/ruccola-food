package kz.ruccola.food.admin.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.ruccola.food.admin.Strings
import kz.ruccola.food.api.DayApi
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.ui.SingleLineText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(onClose: () -> Unit) {
    var days by remember { mutableStateOf<List<DayDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var triggeringMidnight by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val api = remember { DayApi() }

    fun loadDays() {
        scope.launch {
            isLoading = true
            error = null
            try {
                days = api.getAllDays()
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки дней"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadDays() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                triggeringMidnight = true
                                error = null
                                try {
                                    api.triggerMidnight()
                                    loadDays()
                                } catch (e: Exception) {
                                    error = e.message
                                } finally {
                                    triggeringMidnight = false
                                }
                            }
                        },
                        enabled = !triggeringMidnight,
                    ) {
                        Text("Midnight")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { loadDays() }) { Text(Strings.retry) }
                    }
                }

                days.isEmpty() -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Дни не найдены")
                        Spacer(Modifier.height(8.dp))
                        Text("Нажмите Midnight чтобы запустить", style = MaterialTheme.typography.bodySmall)
                    }
                }

                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(days.sortedByDescending { it.date }) { day ->
                            DayItem(day)
                        }
                    }
                }
            }

            if (triggeringMidnight) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Создание следующего дня...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayItem(day: DayDto) {
    val api = remember { DayApi() }
    var dishes by remember { mutableStateOf<List<DishWithMealDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(day.id) {
        isLoading = true
        error = null
        try {
            dishes = api.getDishes(day.id)
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            SingleLineText(day.date.toString(), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.size(18.dp))

                error != null -> Text(
                    text = "Failed to load dishes",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )

                dishes.isEmpty() -> Text(
                    text = "No dishes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dishes.forEach { d: DishWithMealDto ->
                        SingleLineText("• ${d.dish.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
