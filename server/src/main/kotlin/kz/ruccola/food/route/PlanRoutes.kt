package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.api.PlanCreateDto
import kz.ruccola.food.api.PlanUpdateDto
import kz.ruccola.food.api.Plans
import kz.ruccola.food.api.Role
import kz.ruccola.food.service.PlanService
import kz.ruccola.food.withRole

fun Route.configurePlanRoutes() {
    val service = PlanService()

    get<Plans> { call.respond(service.getAll()) }

    withRole(Role.ADMIN) {
        post<Plans> {
            val body = call.receive<PlanCreateDto>()
            if (body.pricePerDay < 0) {
                call.respond(HttpStatusCode.BadRequest, "Invalid values")
                return@post
            }
            if (service.exists(body.calories.value, body.periodDays.amount)) {
                call.respond(HttpStatusCode.Conflict, "A plan with these calories and days already exists")
                return@post
            }
            val created = service.create(body)
            call.respond(HttpStatusCode.Created, created)
        }

        put<Plans.Id> { plan ->
            val body = call.receive<PlanUpdateDto>()
            if (body.pricePerDay == null) {
                call.respond(HttpStatusCode.BadRequest, "At least one field must be provided")
                return@put
            }
            val updated = service.update(plan.id, body)
            if (updated == null) call.respond(HttpStatusCode.NotFound, "Plan not found") else call.respond(updated)
        }

        delete<Plans.Id> { plan ->
            service.delete(plan.id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
