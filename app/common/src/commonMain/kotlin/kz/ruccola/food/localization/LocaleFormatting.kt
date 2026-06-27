package kz.ruccola.food.localization

fun localeTagForFormatting(languageTag: String): String =
    when {
        languageTag.startsWith("kk") -> "kk-KZ"
        languageTag.startsWith("ru") -> "ru-RU"
        else -> "en-US"
    }
