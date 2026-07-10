package kz.ruccola.food.localization

import kotlinx.serialization.Serializable

@Serializable
enum class Language(val dishNamePattern: String) {
    EN("""^(?!\s)[a-zA-Z’ -]+(?<!\s)$"""),
    RU("""^(?!\s)[а-яА-ЯёЁ -]+(?<!\s)$"""),
    KK("""^(?!\s)[а-яА-ЯёЁәғқңөұүһіӘҒҚҢӨҰҮҺІ -]+(?<!\s)$"""),
}
