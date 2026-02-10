package kz.ruccola.food.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlanCalories(
    val value: Int,
) {
    C900(900),
    C1000(1000),
    C1100(1100),
    C1200(1200),
    C1300(1300),
    C1400(1400),
    C1500(1500),
    C1600(1600),
    C1700(1700),
    C1800(1800),
    C1900(1900),
    C2000(2000),
    C2100(2100),
    C2200(2200),
    C2300(2300),
    C2400(2400),
    C2500(2500),
    C2600(2600),
    C2700(2700),
    C2800(2800),
    C2900(2900),
    C3000(3000),
    ;

    companion object {
        fun fromValue(v: Int): PlanCalories = PlanCalories.entries.first { it.value == v }
    }

    val amount: Int
        get() = this.name.removePrefix("C").toInt()
}
