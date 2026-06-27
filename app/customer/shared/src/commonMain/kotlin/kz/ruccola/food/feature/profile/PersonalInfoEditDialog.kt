package kz.ruccola.food.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.address
import food.composeappcustomer.generated.resources.cancel
import food.composeappcustomer.generated.resources.edit_personal_info_title
import food.composeappcustomer.generated.resources.error_prefix
import food.composeappcustomer.generated.resources.first_name
import food.composeappcustomer.generated.resources.last_name
import food.composeappcustomer.generated.resources.save
import food.composeappcustomer.generated.resources.saving
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonalInfoEditDialog(
    viewModel: ProfileViewModel,
    initialFirstName: String,
    initialLastName: String,
    initialAddress: String,
) {
    val uiState by viewModel.uiState.collectAsState()

    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var address by remember { mutableStateOf(initialAddress) }

    AlertDialog(
        onDismissRequest = { viewModel.setEditing(false) },
        title = {
            Text(
                stringResource(Res.string.edit_personal_info_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(stringResource(Res.string.first_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(stringResource(Res.string.last_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringResource(Res.string.address)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.saveError != null) {
                    Text(
                        stringResource(Res.string.error_prefix, uiState.saveError!!),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateCustomer(firstName, lastName, address)
                },
                enabled = !uiState.isSaving,
            ) {
                Text(if (uiState.isSaving) stringResource(Res.string.saving) else stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.setEditing(false) },
                enabled = !uiState.isSaving,
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
