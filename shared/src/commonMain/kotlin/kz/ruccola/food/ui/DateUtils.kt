package kz.ruccola.food.ui

import kotlinx.datetime.LocalDate

expect fun formatDate(
    date: LocalDate,
    locale: String,
): String
