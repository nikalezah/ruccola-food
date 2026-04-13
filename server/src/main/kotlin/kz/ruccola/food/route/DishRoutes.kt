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

            when (val validationResult = validateTranslations(translations, null, dishService)) {
                is ValidationResult.MissingLanguages -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing translations for: ${validationResult.languages.joinToString(", ") { it.name }}",
                    )
                    return@post
                }

                is ValidationResult.EmptyName -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Name cannot be empty for language ${validationResult.language.name}",
                    )
                    return@post
                }

                is ValidationResult.InvalidNameFormat -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid name format for language ${validationResult.language.name}. Only letters and spaces allowed",
                    )
                    return@post
                }

                is ValidationResult.DuplicateName -> {
                    call.respond(
                        HttpStatusCode.Conflict,
                        "A dish with this name already exists in ${validationResult.language.name}",
                    )
                    return@post
                }

                is ValidationResult.Ok -> {}
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
                when (val validationResult = validateTranslations(translations, dish.id, dishService)) {
                    is ValidationResult.MissingLanguages -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Missing translations for: ${validationResult.languages.joinToString(", ") { it.name }}",
                        )
                        return@put
                    }

                    is ValidationResult.EmptyName -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Name cannot be empty for language ${validationResult.language.name}",
                        )
                        return@put
                    }

                    is ValidationResult.InvalidNameFormat -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid name format for language ${validationResult.language.name}. Only letters and spaces allowed",
                        )
                        return@put
                    }

                    is ValidationResult.DuplicateName -> {
                        call.respond(
                            HttpStatusCode.Conflict,
                            "A dish with this name already exists in ${validationResult.language.name}",
                        )
                        return@put
                    }

                    is ValidationResult.Ok -> {}
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
    data object Ok : ValidationResult()

    data class MissingLanguages(
        val languages: List<Language>,
    ) : ValidationResult()

    data class EmptyName(
        val language: Language,
    ) : ValidationResult()

    data class InvalidNameFormat(
        val language: Language,
    ) : ValidationResult()

    data class DuplicateName(
        val language: Language,
    ) : ValidationResult()
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
