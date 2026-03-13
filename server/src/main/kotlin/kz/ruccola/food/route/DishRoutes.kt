package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
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
        get<Dishes> {
            call.respond(dishService.getAll())
        }

        post<Dishes> {
            val payload = call.receive<DishCreateDto>()
            if (payload.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Name cannot be empty")
                return@post
            }
            val dish = dishService.createDish(payload.name, payload.description, payload.imageFileIds)
            call.respond(HttpStatusCode.Created, dish)
        }

        put<Dishes.Id> { dish ->
            val payload = call.receive<DishUpdateDto>()
            if (payload.name.isNullOrBlank() && payload.description.isNullOrBlank() && payload.imageFileIds == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "At least one field (name, description, or images) must be provided for update",
                )
                return@put
            }
            val updated = dishService.updateDish(dish.id, payload.name, payload.description, payload.imageFileIds)
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
