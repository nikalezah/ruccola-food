package kz.ruccola.food

import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.DishTranslations
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Plans
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.UUID

fun uniqueEmail(): String = "user-${UUID.randomUUID()}@test.local"

suspend fun seedDish(
    nameEn: String,
    nameRu: String = nameEn,
    nameKk: String = nameRu,
    descriptionEn: String = "Description",
    descriptionRu: String = "Описание",
    descriptionKk: String = "Сипаттама",
    archived: Boolean = false,
): Int =
    suspendTransaction {
        val dishId = Dishes.insertAndGetId {
            it[Dishes.archived] = archived
        }.value
        Language.entries.forEach { lang ->
            DishTranslations.insert {
                it[DishTranslations.dishId] = dishId
                it[DishTranslations.language] = lang.name
                it[DishTranslations.name] = when (lang) {
                    Language.EN -> nameEn
                    Language.RU -> nameRu
                    Language.KK -> nameKk
                }
                it[DishTranslations.description] = when (lang) {
                    Language.EN -> descriptionEn
                    Language.RU -> descriptionRu
                    Language.KK -> descriptionKk
                }
            }
        }
        dishId
    }

suspend fun seedPlan(
    calories: Int,
    periodDays: Int,
    pricePerDay: Int,
): Int =
    suspendTransaction {
        Plans.insertAndGetId {
            it[Plans.calories] = calories
            it[Plans.periodDays] = periodDays
            it[Plans.pricePerDay] = pricePerDay
        }.value
    }
