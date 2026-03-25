package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.DISH_NAME_PATTERN
import kz.ruccola.food.api.DishCreateDto
import kz.ruccola.food.api.DishUpdateDto
import kz.ruccola.food.api.DishVariantSaveDto
import kz.ruccola.food.api.Dishes
import kz.ruccola.food.api.Role
import kz.ruccola.food.api.VariantCustomersPayload
import kz.ruccola.food.service.DishService
import kz.ruccola.food.withRole

fun Route.configureDishRoutes() {
    val dishService = DishService()

    get<Dishes.Id> { dish ->
        val d = dishService.findById(dish.id) ?: run {
            call.respond(HttpStatusCode.NotFound, "Dish not found")
            return@get
        }
        call.respond(d)
    }

    withRole(Role.ADMIN) {
        get<Dishes.List> { dishes ->
            call.respond(dishService.getAll(dishes.page, dishes.size))
        }

        post<Dishes> {
            val payload = call.receive<DishCreateDto>()
            val name = payload.name.trim()
            if (name.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Name cannot be empty")
                return@post
            }
            if (!Regex(DISH_NAME_PATTERN).matches(name)) {
                call.respond(HttpStatusCode.BadRequest, "Name can only contain letters and spaces")
                return@post
            }
            if (dishService.nameExists(name, excludeId = null)) {
                call.respond(HttpStatusCode.Conflict, "A dish with this name already exists")
                return@post
            }
            val dish = dishService.createDish(name, payload.description, payload.imageFileIds)
            call.respond(HttpStatusCode.Created, dish)
        }

        put<Dishes.Id> { dish ->
            if (!dishService.exists(dish.id)) {
                // todo: remove duplication ↓
                call.respond(HttpStatusCode.NotFound, "Dish not found")
                return@put
            }
            val payload = call.receive<DishUpdateDto>()
            if (payload.name.isNullOrBlank() && payload.description.isNullOrBlank() && payload.imageFileIds == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "At least one field (name, description, or images) must be provided for update",
                )
                return@put
            }
            val trimmedName = payload.name?.trim()
            if (trimmedName != null) {
                if (trimmedName.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Name cannot be empty")
                    return@put
                }
                if (!Regex(DISH_NAME_PATTERN).matches(trimmedName)) {
                    call.respond(HttpStatusCode.BadRequest, "Name can only contain letters and spaces")
                    return@put
                }
                if (dishService.nameExists(trimmedName, excludeId = dish.id)) {
                    call.respond(HttpStatusCode.Conflict, "A dish with this name already exists")
                    return@put
                }
            }
            val updated = dishService.updateDish(dish.id, trimmedName, payload.description, payload.imageFileIds)
                ?: run {
                    // todo: remove duplication ↑
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

        get<Dishes.Id.Variants> { variant ->
            call.respond(dishService.getDishVariants(variant.parent.id))
        }

        post<Dishes.Id.Variants> { variant ->
            val payload = call.receive<DishVariantSaveDto>()
            if (payload.description.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Description cannot be empty")
                return@post
            }
            val created = dishService.addDishVariant(variant.parent.id, payload.description) ?: run {
                call.respond(HttpStatusCode.NotFound, "Dish not found")
                return@post
            }
            call.respond(HttpStatusCode.Created, created)
        }

        put<Dishes.Id.Variants.Id> { variant ->
            val payload = call.receive<DishVariantSaveDto>()
            if (payload.description.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Description cannot be empty")
                return@put
            }
            val updated =
                dishService.updateDishVariant(variant.parent.parent.id, variant.variantId, payload.description)
                    ?: run {
                        call.respond(HttpStatusCode.NotFound, "Variant or dish not found")
                        return@put
                    }
            call.respond(updated)
        }

        delete<Dishes.Id.Variants.Id> { variant ->
            val ok = dishService.deleteDishVariant(variant.parent.parent.id, variant.variantId)
            if (ok) {
                call.respond(HttpStatusCode.OK, "Variant deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Variant or dish not found")
            }
        }

        get<Dishes.Id.Variants.Id.Customers> { customer ->
            call.respond(dishService.getVariantCustomerIds(customer.parent.parent.parent.id, customer.parent.variantId))
        }

        put<Dishes.Id.Variants.Id.Customers> { customer ->
            val payload = call.receive<VariantCustomersPayload>()
            val ok = dishService.setVariantCustomerIds(
                customer.parent.parent.parent.id,
                customer.parent.variantId,
                payload.customerIds,
            )
            if (ok) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound, "Variant or dish not found")
            }
        }
    }
}
