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
        get<Dishes.List> { dishes -> call.respond(dishService.getAll(dishes.page, dishes.size, call.language)) }

        get<Dishes.Id> { dish ->
            val d =
                dishService.findByIdWithTranslations(dish.id)
                    ?: run {
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
            validateTranslations(translations, null, dishService)?.let { (status, message) ->
                call.respond(status, message)
                return@post
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
                validateTranslations(translations, dish.id, dishService)?.let { (status, message) ->
                    call.respond(status, message)
                    return@put
                }
            }
            val updated =
                dishService.updateDish(dish.id, payload.translations, payload.imageFileIds)
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

private suspend fun validateTranslations(
    translations: Map<Language, DishTranslation>,
    excludeId: Int?,
    dishService: DishService,
): Pair<HttpStatusCode, String>? {
    val missingLanguages = Language.entries.filter { it !in translations.keys }
    if (missingLanguages.isNotEmpty()) {
        return HttpStatusCode.BadRequest to
            "Missing translations for: ${missingLanguages.joinToString(", ") { it.name }}"
    }

    for (lang in Language.entries) {
        val translation = translations[lang]!!
        val name = translation.name.trim()
        if (name.isEmpty()) {
            return HttpStatusCode.BadRequest to "Name cannot be empty for language ${lang.name}"
        }
        if (!Regex(lang.dishNamePattern).matches(name)) {
            return HttpStatusCode.BadRequest to
                "Invalid name format for language ${lang.name}. Only letters and spaces allowed"
        }
        if (dishService.translationNameExists(lang, name, excludeId)) {
            return HttpStatusCode.Conflict to "A dish with this name already exists in ${lang.name}"
        }
    }
    return null
}
