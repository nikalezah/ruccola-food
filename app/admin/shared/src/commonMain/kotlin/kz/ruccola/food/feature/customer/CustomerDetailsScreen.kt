package kz.ruccola.food.feature.customer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.back_to_login
import kz.ruccola.food.api.CustomerDetailsDto
import kz.ruccola.food.ui.scaffold.DetailTopBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customer: CustomerDetailsDto,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            DetailTopBar(
                title = "${customer.firstName} ${customer.lastName}",
                onBack = onBack,
                backContentDescription = stringResource(Res.string.back_to_login),
            )
        },
    ) { padding ->
        CustomerDetailsContent(
            customer = customer,
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}
