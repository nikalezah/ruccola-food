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
    private val KEY_THEME_PREFERENCE: Preferences.Key<String> = stringPreferencesKey("app_theme_preference")
    private val KEY_TOKEN: Preferences.Key<String> = stringPreferencesKey("app_token")

    fun languageTagFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_LANGUAGE_TAG]
        }

    fun themePreferenceFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_THEME_PREFERENCE]
        }

    fun tokenFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_TOKEN]
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

    suspend fun setThemePreference(
        context: Context,
        preference: String?,
    ) {
        context.appDataStore.edit { prefs ->
            if (preference.isNullOrBlank()) {
                prefs.remove(KEY_THEME_PREFERENCE)
            } else {
                prefs[KEY_THEME_PREFERENCE] = preference
            }
        }
    }

    suspend fun setToken(
        context: Context,
        token: String?,
    ) {
        context.appDataStore.edit { prefs ->
            if (token.isNullOrBlank()) {
                prefs.remove(KEY_TOKEN)
            } else {
                prefs[KEY_TOKEN] = token
            }
        }
    }
}
