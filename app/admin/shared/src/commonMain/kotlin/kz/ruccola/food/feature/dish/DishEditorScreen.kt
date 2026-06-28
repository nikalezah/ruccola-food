package kz.ruccola.food.feature.dish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.close
import food.composeappadmin.generated.resources.description
import food.composeappadmin.generated.resources.edit_dish
import food.composeappadmin.generated.resources.images
import food.composeappadmin.generated.resources.language_en
import food.composeappadmin.generated.resources.language_kk
import food.composeappadmin.generated.resources.language_ru
import food.composeappadmin.generated.resources.name
import food.composeappadmin.generated.resources.new_dish
import kz.ruccola.food.api.DishWithTranslationsDto
import kz.ruccola.food.localization.Language
import kz.ruccola.food.ui.ApplyIconButton
import kz.ruccola.food.ui.Icons
import kz.ruccola.food.ui.ResponsiveContainer
import kz.ruccola.food.ui.SquareImagesCarousel200
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishEditorScreen(
    initialDish: DishWithTranslationsDto?,
    onClose: () -> Unit,
) {
    val viewModel: DishEditorViewModel = viewModel(
        key = initialDish?.id?.toString(),
        factory = DishEditorViewModel.factory(initialDish),
    )
    val uiState by viewModel.uiState.collectAsState()

    var imageEditorVisible by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(1) }

    val languageTabs = Language.entries.sortedByDescending { it.ordinal }.toTypedArray()
    val languageNames: Map<Language, String> = mapOf(
        Language.KK to stringResource(Res.string.language_kk),
        Language.RU to stringResource(Res.string.language_ru),
        Language.EN to stringResource(Res.string.language_en),
    )

    val allFieldsFilled = Language.entries.all { lang ->
        uiState.translations[lang]?.name?.isNotBlank() == true
    }

    var previousIsBusy by remember { mutableStateOf(false) }
    var previousDish by remember { mutableStateOf(uiState.dish) }

    LaunchedEffect(uiState.isBusy, uiState.dish) {
        val capturedPreviousIsBusy = previousIsBusy
        val capturedPreviousDish = previousDish

        if (!uiState.isBusy && capturedPreviousIsBusy && uiState.error == null) {
            onClose()
        }
        if (initialDish == null && capturedPreviousDish == null && uiState.dish != null) {
            onClose()
        }
        previousIsBusy = uiState.isBusy
        previousDish = uiState.dish
    }

    ResponsiveContainer(maxContentWidth = 640.dp) {
        Scaffold(
            topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.dish == null) Res.string.new_dish else Res.string.edit_dish))
                },
                navigationIcon = {
                    IconButton(onClick = { if (!uiState.isBusy) onClose() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(Res.string.close))
                    }
                },
                actions = {
                    val isNewDish = uiState.dish == null
                    ApplyIconButton(
                        onClick = { viewModel.saveDish() },
                        enabled = allFieldsFilled && !uiState.isBusy && (isNewDish || uiState.hasChanges),
                    )
                },
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(16.dp)) {
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                languageTabs.forEachIndexed { index, lang ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(languageNames[lang] ?: lang.name) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            val currentLanguage = languageTabs[selectedTabIndex]
            OutlinedTextField(
                value = uiState.translations[currentLanguage]?.name ?: "",
                onValueChange = { viewModel.onTranslationNameChange(currentLanguage, it) },
                label = { Text(stringResource(Res.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isBusy,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.translations[currentLanguage]?.description ?: "",
                onValueChange = { viewModel.onTranslationDescriptionChange(currentLanguage, it) },
                label = { Text(stringResource(Res.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !uiState.isBusy,
            )

            if (uiState.dish != null) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.images), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { imageEditorVisible = true }, enabled = !uiState.isBusy) {
                        Icon(Icons.Outlined.EditSquare, contentDescription = "Edit images")
                    }
                }
                Spacer(Modifier.height(8.dp))
                SquareImagesCarousel200(imageUrls = uiState.dish?.images?.map { it.url } ?: emptyList())
            }

            if (uiState.isBusy) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
    }

    if (imageEditorVisible && uiState.dish != null) {
        DishImagesEditorScreen(
            dish = uiState.dish!!,
            onClose = { imageEditorVisible = false },
            onSaved = { updated -> viewModel.onDishUpdated(updated) },
        )
    }
}
