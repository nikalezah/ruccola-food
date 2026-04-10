package kz.ruccola.food.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.ruccola.food.api.DishApi
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishTranslation
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.DishWithTranslationsDto
import kz.ruccola.food.localization.Language

data class DishEditorUiState(
    val dish: DishWithTranslationsDto? = null,
    val translations: Map<Language, DishTranslation> = emptyMap(),
    val isBusy: Boolean = false,
    val error: String? = null,
)

class DishEditorViewModel(
    initialDish: DishWithTranslationsDto?,
) : ViewModel() {
    private val dishApi = DishApi()

    private fun getInitialTranslations(dish: DishWithTranslationsDto?): Map<Language, DishTranslation> {
        if (dish == null) {
            return Language.entries.associateWith { DishTranslation("", "") }
        }
        return Language.entries.associateWith { lang ->
            dish.translations[lang] ?: DishTranslation("", "")
        }
    }

    val uiState: StateFlow<DishEditorUiState>
        field = MutableStateFlow(
            DishEditorUiState(
                dish = initialDish,
                translations = getInitialTranslations(initialDish),
            ),
        )

    fun saveDish() {
        viewModelScope.launch {
            uiState.update { it.copy(isBusy = true, error = null) }
            try {
                val currentDish = uiState.value.dish
                val translations = uiState.value.translations

                val missingFields = Language.entries.filter { lang ->
                    val t = translations[lang]
                    t?.name.isNullOrBlank()
                }
                if (missingFields.isNotEmpty()) {
                    uiState.update {
                        it.copy(
                            isBusy = false,
                            error = "Please fill in name for all languages: ${
                                missingFields.joinToString(", ") {
                                    it.name
                                }
                            }",
                        )
                    }
                    return@launch
                }

                if (currentDish == null) {
                    val created = dishApi.createDish(
                        DishCreateDto(translations = translations),
                    )
                    uiState.update {
                        it.copy(
                            dish = created,
                            translations = created.translations,
                            isBusy = false,
                        )
                    }
                } else {
                    val updated = dishApi.updateDish(
                        currentDish.id,
                        DishUpdateDto(translations = translations),
                    )
                    uiState.update {
                        it.copy(
                            dish = updated,
                            translations = updated.translations,
                            isBusy = false,
                        )
                    }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(error = e.message ?: "Ошибка сохранения", isBusy = false) }
            }
        }
    }

    fun onTranslationNameChange(
        language: Language,
        name: String,
    ) {
        uiState.update { state ->
            val currentTranslation = state.translations[language] ?: DishTranslation("", "")
            state.copy(
                translations = state.translations + (language to currentTranslation.copy(name = name)),
            )
        }
    }

    fun onTranslationDescriptionChange(
        language: Language,
        description: String,
    ) {
        uiState.update { state ->
            val currentTranslation = state.translations[language] ?: DishTranslation("", "")
            state.copy(
                translations = state.translations + (language to currentTranslation.copy(description = description)),
            )
        }
    }

    fun onDishUpdated(updated: DishWithTranslationsDto) {
        uiState.update { it.copy(dish = updated, translations = updated.translations) }
    }
}
