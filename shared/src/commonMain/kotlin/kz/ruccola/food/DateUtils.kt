package kz.ruccola.food

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

fun formatDate(
    date: LocalDate,
    locale: String,
): String {
    val dayOfWeekName = when (locale) {
        "en-US" -> when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
        }

        "ru-RU" -> when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Понедельник"
            DayOfWeek.TUESDAY -> "Вторник"
            DayOfWeek.WEDNESDAY -> "Среда"
            DayOfWeek.THURSDAY -> "Четверг"
            DayOfWeek.FRIDAY -> "Пятница"
            DayOfWeek.SATURDAY -> "Суббота"
            DayOfWeek.SUNDAY -> "Воскресенье"
        }

        "kk-KZ" -> when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Дүйсенбі"
            DayOfWeek.TUESDAY -> "Сейсенбі"
            DayOfWeek.WEDNESDAY -> "Сәрсенбі"
            DayOfWeek.THURSDAY -> "Бейсенбі"
            DayOfWeek.FRIDAY -> "Жұма"
            DayOfWeek.SATURDAY -> "Сенбі"
            DayOfWeek.SUNDAY -> "Жексенбі"
        }

        else -> throw IllegalArgumentException("Unsupported locale: $locale")
    }

    val day = date.day.toString().padStart(2, '0')
    val month = date.month.number.toString().padStart(2, '0')

    return when (locale) {
        "en-US" -> "$dayOfWeekName, $month/$day"
        else -> "$dayOfWeekName, $day.$month"
    }
}
