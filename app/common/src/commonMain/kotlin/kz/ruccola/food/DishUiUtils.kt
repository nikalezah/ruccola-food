package kz.ruccola.food

fun dishImageUrl(url: String): String {
    val base = BASE_URL
    return if (url.startsWith("/")) base + url else url
}
