package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.CustomerUpdateDto
import kz.ruccola.food.api.Customers
import kz.ruccola.food.api.PagingResponse
import kz.ruccola.food.api.Role
import kz.ruccola.food.api.ScheduledDayDto
import kz.ruccola.food.service.CustomerService
import kz.ruccola.food.service.MealPlanDayService
import kz.ruccola.food.today
import kz.ruccola.food.user
import kz.ruccola.food.withRole

fun Route.configureCustomerRoutes() {
    val customerService = CustomerService()
    val mpdService = MealPlanDayService()

    withRole(Role.ADMIN) {
        get<Customers> {
            call.respond(customerService.findAllWithDetails())
        }
    }

    withRole(Role.CUSTOMER) {
        get<Customers.Profile> {
            call.respond(customerService.findById(call.user.id)!!)
        }

        put<Customers.Profile> {
            val req = call.receive<CustomerUpdateDto>()
            // Simple validation: if provided, they must not be blank strings
            val cleanedFirst = req.firstName?.trim()?.takeIf { it.isNotEmpty() }
            val cleanedLast = req.lastName?.trim()?.takeIf { it.isNotEmpty() }
            val cleanedAddr = req.address?.trim()?.takeIf { it.isNotEmpty() }

            val customer = customerService.update(call.user.id, cleanedFirst, cleanedLast, cleanedAddr)
            if (customer == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@put
            }
            call.respond(customer)
        }

        get<Customers.Plan> {
            val customerPlan = customerService.getCustomerPlan(call.user.id)
            if (customerPlan == null) {
                call.respond(HttpStatusCode.NotFound, "No plan found for customer")
            } else {
                call.respond(customerPlan)
            }
        }

        post<Customers.Plan> {
            val body = call.receive<CustomerPlanCreateDto>()
            try {
                val saved = customerService.saveCustomerPlan(call.user.id, body)
                call.respond(HttpStatusCode.Created, saved)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        get<Customers.Schedule> { schedule ->
            val all = mpdService.getAll()
            // Determine starting MPD: current or first by serial
            val start = mpdService.getCurrent() ?: all.firstOrNull()

            val today = today()
            val result = mutableListOf<ScheduledDayDto>()
            if (all.isEmpty()) {
                // Return dates with empty dishes if there are no meal plan days
                repeat(schedule.size) { i ->
                    result.add(ScheduledDayDto(date = today.plus(i, DateTimeUnit.DAY), dishes = emptyList()))
                }
                call.respond(
                    PagingResponse(
                        items = result,
                        totalCount = schedule.size.toLong(),
                        page = schedule.page,
                        size = schedule.size,
                    ),
                )
                return@get
            }
            val ordered = all.sortedBy { it.serial }
            val startIndex = start
                ?.let { s -> ordered.indexOfFirst { it.id == s.id } }
                .takeIf { it != null && it >= 0 }
                ?: 0

            var idx = startIndex
            repeat(schedule.size) { i ->
                val mpd = ordered[idx]
                val dishes = mpdService.getDishes(mpd.id).getOrElse { emptyList() }
                result.add(ScheduledDayDto(date = today.plus(i, DateTimeUnit.DAY), dishes = dishes))
                idx = (idx + 1) % ordered.size
            }
            call.respond(
                PagingResponse(
                    items = result,
                    totalCount = schedule.size.toLong(),
                    page = schedule.page,
                    size = schedule.size,
                ),
            )
        }
    }
}
