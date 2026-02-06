package kz.ruccola.food

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform