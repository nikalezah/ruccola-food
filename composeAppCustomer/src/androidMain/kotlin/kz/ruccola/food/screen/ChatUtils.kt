package kz.ruccola.food.screen

fun parseUserId(token: String): Int? {
    if (!token.startsWith("dummy-token-")) return null
    return token.split("-").lastOrNull()?.toIntOrNull()
}
