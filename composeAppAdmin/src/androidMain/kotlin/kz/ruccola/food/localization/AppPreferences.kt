package kz.ruccola.food.localization

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val APP_PREFS_NAME = "app_prefs"

val Context.appDataStore by preferencesDataStore(name = APP_PREFS_NAME)

object AppPreferences {
    private val KEY_LANGUAGE_TAG: Preferences.Key<String> = stringPreferencesKey("app_language_tag")

    fun languageTagFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_LANGUAGE_TAG]
        }

    suspend fun setLanguageTag(
        context: Context,
        tag: String?,
    ) {
        context.appDataStore.edit { prefs ->
            if (tag.isNullOrBlank()) {
                prefs.remove(KEY_LANGUAGE_TAG)
            } else {
                prefs[KEY_LANGUAGE_TAG] = tag
            }
        }
    }
}
