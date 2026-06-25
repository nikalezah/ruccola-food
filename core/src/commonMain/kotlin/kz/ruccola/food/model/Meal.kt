package kz.ruccola.food.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
enum class Meal(
    val time: LocalTime,
) {
    BREAKFAST(LocalTime(8, 0)),
    BRUNCH(LocalTime(11, 0)),
    LAUNCH(LocalTime(13, 30)),
    AFTERNOON_SNACK(LocalTime(16, 0)),
    DINNER(LocalTime(18, 30)),
}

/* // todo: fix shared module dependencies issue and implement localization of Meal with @StringRes
@Serializable
enum class Meal (
    @StringRes val labelResId: Int,
    val time: LocalTime
) {
    BREAKFAST(R.string.meal_breakfast, LocalTime(8, 0)),
    BRUNCH(R.string.meal_brunch, LocalTime(11, 0)),
    LAUNCH(R.string.meal_lunch, LocalTime(13, 30)),
    AFTERNOON_SNACK(R.string.meal_afternoon_snack, LocalTime(14, 0)),
    DINNER(R.string.meal_dinner, LocalTime(18, 30))
}
*/
