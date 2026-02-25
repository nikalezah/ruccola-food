package kz.ruccola.food.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.address
import food.composeappadmin.generated.resources.back_to_login
import food.composeappadmin.generated.resources.calories
import food.composeappadmin.generated.resources.email
import food.composeappadmin.generated.resources.first_name
import food.composeappadmin.generated.resources.last_name
import food.composeappadmin.generated.resources.no_data
import kz.ruccola.food.api.CustomerDto
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customer: CustomerDto,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${customer.firstName} ${customer.lastName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_to_login),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CustomerDetailRow(label = stringResource(Res.string.first_name), value = customer.firstName)
            CustomerDetailRow(label = stringResource(Res.string.last_name), value = customer.lastName)
            CustomerDetailRow(label = stringResource(Res.string.email), value = customer.email)
            CustomerDetailRow(
                label = stringResource(Res.string.address),
                value = customer.address.ifBlank {
                    stringResource(Res.string.no_data)
                },
            )
            CustomerDetailRow(
                label = stringResource(Res.string.calories),
                value = customer.calories?.toString() ?: stringResource(Res.string.no_data),
            )
        }
    }
}

@Composable
private fun CustomerDetailRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
