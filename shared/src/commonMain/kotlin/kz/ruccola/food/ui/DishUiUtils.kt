package kz.ruccola.food.ui

import kz.ruccola.food.BASE_URL

fun dishImageUrl(url: String): String {
    val base = BASE_URL
    return if (url.startsWith("/")) base + url else url
}
