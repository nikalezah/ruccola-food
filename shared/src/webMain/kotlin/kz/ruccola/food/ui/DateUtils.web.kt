package kz.ruccola.food.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

actual fun formatDate(
    date: LocalDate,
    locale: String,
): String {
    val dayOfMonth = date.day.toString().padStart(2, '0')
    val monthNumber = date.month.number.toString().padStart(2, '0')
    return "$dayOfMonth.$monthNumber.${date.year}"
}
