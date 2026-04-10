package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.api.MealPlanDaySaveDto
import kz.ruccola.food.api.MealPlanDays
import kz.ruccola.food.api.MealPlanDaysReorderDto
import kz.ruccola.food.api.Role
import kz.ruccola.food.language
import kz.ruccola.food.service.MealPlanDayService
import kz.ruccola.food.withRole

fun Route.configureMealPlanDayRoutes() {
    val service = MealPlanDayService()

    withRole(Role.ADMIN) {
        get<MealPlanDays> {
            val list = service.getAll(call.language)
            call.respond(list)
        }

        put<MealPlanDays> {
            val body = call.receive<MealPlanDaySaveDto>()
            val result = service.save(body.id, body.dishIdToMeal, call.language)
            result.fold(
                onSuccess = { call.respond(it) },
                onFailure = {
                    val status = when (it) {
                        is NoSuchElementException -> HttpStatusCode.NotFound
                        is IllegalArgumentException -> HttpStatusCode.BadRequest
                        is IllegalStateException -> HttpStatusCode.Conflict
                        else -> HttpStatusCode.InternalServerError
                    }
                    call.respond(status, it.message ?: "Error updating meal plan day")
                },
            )
        }

        delete<MealPlanDays.Id> { mealPlanDay ->
            service.delete(mealPlanDay.id)
            call.respond(HttpStatusCode.OK)
        }

        post<MealPlanDays.Id.Current> { current ->
            val result = service.setCurrent(current.parent.id)
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = {
                    val status =
                        if (it is NoSuchElementException) {
                            HttpStatusCode.NotFound
                        } else {
                            HttpStatusCode.InternalServerError
                        }
                    call.respond(status, it.message ?: "Error setting current meal plan day")
                },
            )
        }

        post<MealPlanDays.Reorder> {
            val body = call.receive<MealPlanDaysReorderDto>()
            val result = service.reorder(body.ids)
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.OK) },
                onFailure = { e ->
                    val status = when (e) {
                        is IllegalArgumentException -> HttpStatusCode.BadRequest
                        is NoSuchElementException -> HttpStatusCode.NotFound
                        else -> HttpStatusCode.InternalServerError
                    }
                    call.respond(status, e.message ?: "Error applying new order")
                },
            )
        }
    }
}
