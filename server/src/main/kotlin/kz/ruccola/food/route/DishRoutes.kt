package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.Dishes
import kz.ruccola.food.api.Role
import kz.ruccola.food.language
import kz.ruccola.food.localization.Language
import kz.ruccola.food.service.DishService
import kz.ruccola.food.withRole

fun Route.configureDishRoutes() {
    val dishService = DishService()

    get<Dishes.Id> { dish ->
        val d = dishService.findById(dish.id, call.language) ?: run {
            call.respond(HttpStatusCode.NotFound, "Dish not found")
            return@get
        }
        call.respond(d)
    }

    withRole(Role.ADMIN) {
        get<Dishes.List> { dishes ->
            call.respond(dishService.getAllWithTranslations(dishes.page, dishes.size))
        }

        post<Dishes> {
            val payload = call.receive<DishCreateDto>()
            val translations = payload.translations

            if (translations.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "At least one translation is required")
                return@post
            }

            val missingLanguages = Language.entries.filter { it !in translations.keys }
            if (missingLanguages.isNotEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing translations for: ${missingLanguages.joinToString(", ") { it.name }}",
                )
                return@post
            }

            for (lang in Language.entries) {
                val translation = translations[lang]!!
                val name = translation.name.trim()
                if (name.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Name cannot be empty for language ${lang.name}")
                    return@post
                }
                if (!Regex(lang.dishNamePattern).matches(name)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Invalid name format for language ${lang.name}. Only letters and spaces allowed",
                    )
                    return@post
                }
                if (dishService.translationNameExists(lang, name, excludeId = null)) {
                    call.respond(HttpStatusCode.Conflict, "A dish with this name already exists in ${lang.name}")
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
                val missingLanguages = Language.entries.filter { it !in translations.keys }
                if (missingLanguages.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing translations for: ${missingLanguages.joinToString(", ") { it.name }}",
                    )
                    return@put
                }

                for (lang in Language.entries) {
                    val translation = translations[lang]!!
                    val name = translation.name.trim()
                    if (name.isEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, "Name cannot be empty for language ${lang.name}")
                        return@put
                    }
                    if (!Regex(lang.dishNamePattern).matches(name)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Invalid name format for language ${lang.name}. Only letters and spaces allowed",
                        )
                        return@put
                    }
                    if (dishService.translationNameExists(lang, name, excludeId = dish.id)) {
                        call.respond(HttpStatusCode.Conflict, "A dish with this name already exists in ${lang.name}")
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
