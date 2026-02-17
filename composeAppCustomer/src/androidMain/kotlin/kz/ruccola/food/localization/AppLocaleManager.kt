package kz.ruccola.food.localization

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

object AppLocaleManager {
    val supportedLanguageTags = listOf("en", "ru", "kk")

    /** Apply stored language preference synchronously (use early in-app start). */
    fun applyStoredLanguage(context: Context) {
        val tag: String? = runBlocking { AppPreferences.languageTagFlow(context).first() }
        if (!tag.isNullOrBlank()) {
            applyLanguageInternal(context, tag)
        }
    }

    /** Persist and apply a new language tag (e.g., "en", "ru", "kk"). */
    suspend fun setLanguage(
        context: Context,
        languageTag: String,
    ) {
        AppPreferences.setLanguageTag(context, languageTag)
        applyLanguageInternal(context, languageTag)
    }

    /** Return the currently applied app locale if set, otherwise system default. */
    fun getCurrentLocale(context: Context): Locale =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val lm = context.getSystemService(LocaleManager::class.java)
            val list = lm.applicationLocales
            if (list.isEmpty) Locale.getDefault() else list[0]
        } else {
            val list = AppCompatDelegate.getApplicationLocales()
            if (list.isEmpty) Locale.getDefault() else list[0] ?: Locale.getDefault()
        }

    /** Return the currently applied app language tag if set, otherwise system default. */
    fun getCurrentLanguageTag(context: Context): String = getCurrentLocale(context).toLanguageTag()

    private fun applyLanguageInternal(
        context: Context,
        languageTag: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use platform-per-app language API on API 33+
            val localeList = LocaleList.forLanguageTags(languageTag)
            val lm = context.getSystemService(LocaleManager::class.java)
            lm.applicationLocales = localeList
        }
        // Always set AppCompat delegate to cover <33 and ensure Compose picks it up
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
