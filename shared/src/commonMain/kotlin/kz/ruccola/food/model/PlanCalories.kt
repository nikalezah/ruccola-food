package kz.ruccola.food.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlanCalories(
    val value: Int,
) {
    C900(900),
    C1200(1200),
    C1500(1500),
    C1800(1800),
    C2200(2200),
    C2600(2600),
    ;

    companion object {
        fun fromValue(v: Int): PlanCalories = PlanCalories.entries.first { it.value == v }
    }

    val amount: Int
        get() = this.name.removePrefix("C").toInt()
}
