package kz.ruccola.food.localization

import android.content.Context
import kz.ruccola.food.R
import kz.ruccola.food.model.Meal

fun Meal.toLocalizedString(context: Context) =
    when (this) {
        Meal.BREAKFAST -> context.getString(R.string.meal_breakfast)
        Meal.BRUNCH -> context.getString(R.string.meal_brunch)
        Meal.LAUNCH -> context.getString(R.string.meal_lunch)
        Meal.AFTERNOON_SNACK -> context.getString(R.string.meal_afternoon_snack)
        Meal.DINNER -> context.getString(R.string.meal_dinner)
    }
