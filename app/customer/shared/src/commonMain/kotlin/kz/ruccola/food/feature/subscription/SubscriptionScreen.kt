package kz.ruccola.food.feature.subscription

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.tab_subscription
import kz.ruccola.food.ui.ResponsiveContainer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsState()

    ResponsiveContainer(maxContentWidth = 640.dp) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.tab_subscription)) },
                )
            },
        ) { padding ->
            SubscriptionContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}
