package kz.ruccola.food.theme

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        fun fromStorage(value: String?): ThemePreference =
            when (value?.lowercase()) {
                "light" -> LIGHT
                "dark" -> DARK
                else -> SYSTEM
            }
    }

    fun storageValue(): String = name.lowercase()
}
