package kz.ruccola.food.screen

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.click_midnight_to_start
import food.composeappadmin.generated.resources.close
import food.composeappadmin.generated.resources.error_prefix
import food.composeappadmin.generated.resources.failed_to_load_dishes
import food.composeappadmin.generated.resources.no_days_found
import food.composeappadmin.generated.resources.no_dishes
import food.composeappadmin.generated.resources.retry
import food.composeappadmin.generated.resources.screen_history_title
import food.composeappadmin.generated.resources.triggering_midnight
import kz.ruccola.food.api.DayApi
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.api.DishWithMealDto
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.DayViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(onClose: () -> Unit) {
    val vm: DayViewModel = viewModel(factory = DayViewModel.Factory)
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(Res.string.close))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { vm.triggerMidnight() },
                        enabled = !state.isTriggeringMidnight,
                    ) {
                        Text("Midnight")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { vm.loadDays() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading && state.days.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(Res.string.error_prefix, state.error ?: ""),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.loadDays() }) { Text(stringResource(Res.string.retry)) }
                    }
                }

                state.days.isEmpty() -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.no_days_found))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(Res.string.click_midnight_to_start),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.days) { day ->
                            DayItem(day)
                        }
                    }
                }
            }

            if (state.isTriggeringMidnight) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(Res.string.triggering_midnight))
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
                    text = stringResource(Res.string.failed_to_load_dishes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )

                dishes.isEmpty() -> Text(
                    text = stringResource(Res.string.no_dishes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dishes.forEach { d ->
                        SingleLineText("• ${d.dish.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
