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
    private val KEY_THEME: Preferences.Key<String> = stringPreferencesKey("app_theme")
    private val KEY_ROLE: Preferences.Key<String> = stringPreferencesKey("app_role")
    private val KEY_TOKEN: Preferences.Key<String> = stringPreferencesKey("app_token")

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

    fun themeFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_THEME]
        }

    suspend fun setTheme(
        context: Context,
        theme: String?,
    ) {
        context.appDataStore.edit { prefs ->
            if (theme.isNullOrBlank()) {
                prefs.remove(KEY_THEME)
            } else {
                prefs[KEY_THEME] = theme
            }
        }
    }

    fun roleFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_ROLE]
        }

    suspend fun setRole(
        context: Context,
        role: String?,
    ) {
        context.appDataStore.edit { prefs ->
            if (role.isNullOrBlank()) {
                prefs.remove(KEY_ROLE)
            } else {
                prefs[KEY_ROLE] = role
            }
        }
    }

    fun tokenFlow(context: Context): Flow<String?> =
        context.appDataStore.data.map { prefs ->
            prefs[KEY_TOKEN]
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
