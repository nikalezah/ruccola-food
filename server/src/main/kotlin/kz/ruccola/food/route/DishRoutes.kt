package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishTranslation
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.Dishes
import kz.ruccola.food.api.Role
import kz.ruccola.food.language
import kz.ruccola.food.localization.Language
import kz.ruccola.food.service.DishService
import kz.ruccola.food.withRole

fun Route.configureDishRoutes() {
    val dishService = DishService()

    withRole(Role.ADMIN) {
        get<Dishes.List> { dishes ->
            call.respond(dishService.getAll(dishes.page, dishes.size, call.language))
        }

        get<Dishes.Id> { dish ->
            val d = dishService.findByIdWithTranslations(dish.id) ?: run {
                call.respond(HttpStatusCode.NotFound, "Dish not found")
                return@get
            }
            call.respond(d)
        }

        post<Dishes> {
            val payload = call.receive<DishCreateDto>()
            val translations = payload.translations

            if (translations.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "At least one translation is required")
                return@post
            }

            when (val result = validateTranslations(translations, null, dishService)) {
                is ValidationResult.Ok -> {}

                else -> {
                    call.respond(result.status, result.message)
                    return@post
                }
            }

            val dish = dishService.createDish(translations, payload.imageFileIds)
            call.respond(HttpStatusCode.Created, dish)
        }

        put<Dishes.Id> { dish ->
            if (!dishService.exists(dish.id)) {
                call.respond(HttpStatusCode.NotFound, "Dish not found")
                return@put
            }
            val payload = call.receive<DishUpdateDto>()
            if (payload.translations == null && payload.imageFileIds == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "At least one field (translations or images) must be provided for update",
                )
                return@put
            }

            payload.translations?.let { translations ->
                when (val result = validateTranslations(translations, dish.id, dishService)) {
                    is ValidationResult.Ok -> {}

                    else -> {
                        call.respond(result.status, result.message)
                        return@put
                    }
                }
            }

            val updated = dishService.updateDish(dish.id, payload.translations, payload.imageFileIds)
                ?: run {
                    call.respond(HttpStatusCode.NotFound, "Dish not found")
                    return@put
                }
            call.respond(updated)
        }

        post<Dishes.Id.Archive> { archive ->
            val success = dishService.archiveDish(archive.parent.id)
            if (!success) {
                call.respond(HttpStatusCode.NotFound, "Dish not found")
                return@post
            }
            call.respond(HttpStatusCode.OK, "Dish archived successfully")
        }
    }
}

private sealed class ValidationResult {
    abstract val status: HttpStatusCode
    abstract val message: String

    data object Ok : ValidationResult() {
        override val status = HttpStatusCode.OK
        override val message = ""
    }

    data class MissingLanguages(
        val languages: List<Language>,
    ) : ValidationResult() {
        override val status = HttpStatusCode.BadRequest
        override val message = "Missing translations for: ${languages.joinToString(", ") { it.name }}"
    }

    data class EmptyName(
        val language: Language,
    ) : ValidationResult() {
        override val status = HttpStatusCode.BadRequest
        override val message = "Name cannot be empty for language ${language.name}"
    }

    data class InvalidNameFormat(
        val language: Language,
    ) : ValidationResult() {
        override val status = HttpStatusCode.BadRequest
        override val message = "Invalid name format for language ${language.name}. Only letters and spaces allowed"
    }

    data class DuplicateName(
        val language: Language,
    ) : ValidationResult() {
        override val status = HttpStatusCode.Conflict
        override val message = "A dish with this name already exists in ${language.name}"
    }
}

private suspend fun validateTranslations(
    translations: Map<Language, DishTranslation>,
    excludeId: Int?,
    dishService: DishService,
): ValidationResult {
    val missingLanguages = Language.entries.filter { it !in translations.keys }
    if (missingLanguages.isNotEmpty()) {
        return ValidationResult.MissingLanguages(missingLanguages)
    }

    for (lang in Language.entries) {
        val translation = translations[lang]!!
        val name = translation.name.trim()
        if (name.isEmpty()) {
            return ValidationResult.EmptyName(lang)
        }
        if (!Regex(lang.dishNamePattern).matches(name)) {
            return ValidationResult.InvalidNameFormat(lang)
        }
        if (dishService.translationNameExists(lang, name, excludeId)) {
            return ValidationResult.DuplicateName(lang)
        }
    }
    return ValidationResult.Ok
}
