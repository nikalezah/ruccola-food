package kz.ruccola.food.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlanDays(
    val amount: Int,
) {
    D1(1),
    D7(7),
    D14(14),
    D21(21),
    D30(30),
    ;

    companion object {
        fun fromDays(d: Int): PlanDays = PlanDays.entries.first { it.amount == d }
    }
}
