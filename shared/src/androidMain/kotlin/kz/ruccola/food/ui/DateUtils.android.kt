package kz.ruccola.food.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.util.Calendar
import java.util.Locale

actual fun formatDate(
    date: LocalDate,
    locale: String,
): String {
    val javaLocale = Locale.forLanguageTag(locale)
    val calendar = Calendar.getInstance(javaLocale)
    calendar.set(date.year, date.month.number - 1, date.day)

    val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, javaLocale) ?: ""
    val dayOfMonth = date.day.toString().padStart(2, '0')
    val monthNumber = date.month.number.toString().padStart(2, '0')
    val year = date.year

    return "$dayOfWeek, $dayOfMonth.$monthNumber.$year".replaceFirstChar { it.titlecase(javaLocale) }
}
