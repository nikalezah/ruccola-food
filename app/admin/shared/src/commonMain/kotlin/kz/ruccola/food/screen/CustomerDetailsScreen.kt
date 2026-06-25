package kz.ruccola.food.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
            SectionCard(
                icon = Icons.Outlined.ManageAccounts,
                title = "Personal Info",
            ) {
                DetailRow(
                    label = stringResource(Res.string.first_name),
                    value = customer.firstName,
                )
                HorizontalDivider()
                DetailRow(
                    label = stringResource(Res.string.last_name),
                    value = customer.lastName,
                )
                HorizontalDivider()
                DetailRow(
                    label = stringResource(Res.string.email),
                    value = customer.email,
                )
                HorizontalDivider()
                DetailRow(
                    label = stringResource(Res.string.address),
                    value = customer.address.ifBlank { stringResource(Res.string.no_data) },
                )
            }

            val plan = customer.plan
            if (plan != null) {
                SectionCard(
                    icon = Icons.Filled.CalendarMonth,
                    title = stringResource(Res.string.subscription),
                ) {
                    DetailRow(
                        label = stringResource(Res.string.calories),
                        value = plan.calories.toString(),
                    )
                    HorizontalDivider()
                    DetailRow(
                        label = stringResource(Res.string.period),
                        value = stringResource(Res.string.days_count, plan.days),
                    )
                    HorizontalDivider()
                    DetailRow(
                        label = stringResource(Res.string.price_per_day_short),
                        value = plan.pricePerDay.toString(),
                    )
                    HorizontalDivider()
                    DetailRow(
                        label = stringResource(Res.string.total),
                        value = (plan.pricePerDay * plan.days).toString(),
                    )
                    HorizontalDivider()
                    DetailRow(
                        label = stringResource(Res.string.start_date),
                        value = plan.chosenDate.toString(),
                    )
                }
                SectionCard(
                    icon = Icons.Outlined.Settings,
                    title = stringResource(Res.string.preferences),
                ) {
                    PrefRow(
                        label = stringResource(Res.string.needs_cutlery),
                        value = customer.prefs.needsCutlery,
                    )
                    HorizontalDivider()
                    PrefRow(
                        label = stringResource(Res.string.weekend_delivery),
                        value = customer.prefs.weekendDelivery,
                    )
                    HorizontalDivider()
                    PrefRow(
                        label = stringResource(Res.string.morning_delivery),
                        value = customer.prefs.morningDelivery,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PrefRow(
    label: String,
    value: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (value) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = if (value) Icons.Filled.Check else Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp).size(16.dp),
                    tint = if (value) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = stringResource(if (value) Res.string.yes else Res.string.no),
                style = MaterialTheme.typography.bodyMedium,
                color = if (value) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
