package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.address
import food.composeappadmin.generated.resources.back_to_login
import food.composeappadmin.generated.resources.calories
import food.composeappadmin.generated.resources.days_count
import food.composeappadmin.generated.resources.email
import food.composeappadmin.generated.resources.first_name
import food.composeappadmin.generated.resources.last_name
import food.composeappadmin.generated.resources.morning_delivery
import food.composeappadmin.generated.resources.needs_cutlery
import food.composeappadmin.generated.resources.no
import food.composeappadmin.generated.resources.no_data
import food.composeappadmin.generated.resources.period
import food.composeappadmin.generated.resources.preferences
import food.composeappadmin.generated.resources.price_per_day_short
import food.composeappadmin.generated.resources.start_date
import food.composeappadmin.generated.resources.subscription
import food.composeappadmin.generated.resources.total
import food.composeappadmin.generated.resources.weekend_delivery
import food.composeappadmin.generated.resources.yes
import kz.ruccola.food.api.CustomerDetailsDto
import kz.ruccola.food.ui.Icons
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customer: CustomerDetailsDto,
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
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_to_login),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle(stringResource(Res.string.first_name))
                    DetailValue(customer.firstName)
                    SectionTitle(stringResource(Res.string.last_name))
                    DetailValue(customer.lastName)
                    SectionTitle(stringResource(Res.string.email))
                    DetailValue(customer.email)
                    SectionTitle(stringResource(Res.string.address))
                    DetailValue(customer.address.ifBlank { stringResource(Res.string.no_data) })
                }
            }

            val plan = customer.plan
            if (plan != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SectionTitle(stringResource(Res.string.subscription))
                        DetailRow(
                            label = stringResource(Res.string.calories),
                            value = plan.calories.toString(),
                        )
                        DetailRow(
                            label = stringResource(Res.string.period),
                            value = stringResource(Res.string.days_count, plan.days),
                        )
                        DetailRow(
                            label = stringResource(Res.string.price_per_day_short),
                            value = plan.pricePerDay.toString(),
                        )
                        DetailRow(
                            label = stringResource(Res.string.total),
                            value = (plan.pricePerDay * plan.days).toString(),
                        )
                        DetailRow(
                            label = stringResource(Res.string.start_date),
                            value = plan.chosenDate.toString(),
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SectionTitle(stringResource(Res.string.preferences))
                        PrefRow(
                            label = stringResource(Res.string.needs_cutlery),
                            value = customer.prefs.needsCutlery,
                        )
                        PrefRow(
                            label = stringResource(Res.string.weekend_delivery),
                            value = customer.prefs.weekendDelivery,
                        )
                        PrefRow(
                            label = stringResource(Res.string.morning_delivery),
                            value = customer.prefs.morningDelivery,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DetailValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PrefRow(
    label: String,
    value: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(if (value) Res.string.yes else Res.string.no),
            style = MaterialTheme.typography.bodyLarge,
            color = if (value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
