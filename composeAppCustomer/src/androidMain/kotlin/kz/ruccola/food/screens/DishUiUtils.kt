package kz.ruccola.food.screens

import kz.ruccola.food.BASE_URL

// Helper to make sure image URLs are absolute for Coil
fun dishImageUrl(url: String): String {
    val base = BASE_URL
    return if (url.startsWith("/")) base + url else url
}
