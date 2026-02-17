package kz.ruccola.food.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kz.ruccola.food.api.DayDto
import kz.ruccola.food.api.DishDto
import kz.ruccola.food.repository.DayRepository
import kz.ruccola.food.ui.SingleLineText
import kz.ruccola.food.viewmodel.DayViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AndroidDayScreen(onClose: () -> Unit) {
    val vm: DayViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val ctx = LocalContext.current

    val ptrState = rememberPullToRefreshState()
    val threshold = 100.dp
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val targetY = if (state.isLoading) 0f else (ptrState.distanceFraction * thresholdPx)
    val animatedTranslationY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = spring(),
        label = "bounce_animation",
    )
    LaunchedEffect(ptrState.distanceFraction) {
        if (ptrState.distanceFraction >= 1f && !state.isLoading) {
            vm.loadDays()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    IconButton(onClick = { onClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { vm.triggerMidnight() }) {
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
            state = ptrState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = ptrState,
                    isRefreshing = state.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp)
                    .graphicsLayer { translationY = animatedTranslationY },
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.days) { day ->
                    AndroidDayItem(day)
                }
            }
        }
    }
}

@Composable
fun AndroidDayItem(day: DayDto) {
    val repository = remember { DayRepository() }
    var dishes by remember { mutableStateOf<List<DishDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(day.id) {
        isLoading = true
        error = null
        val result = repository.getDishes(day.id)
        result.onSuccess { list -> dishes = list }.onFailure { e -> error = e.message }
        isLoading = false
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
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
                    dishes.forEach { d ->
                        SingleLineText("• ${d.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
