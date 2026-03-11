package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
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
import kz.ruccola.food.api.UserDto
import kz.ruccola.food.api.WeeklyPlanDayDto
import kz.ruccola.food.service.CustomerService
import kz.ruccola.food.service.MealPlanDayService
import kz.ruccola.food.service.UserService
import kz.ruccola.food.today

fun Route.configureCustomerRoutes(userService: UserService) {
    val customerService = CustomerService()
    val mpdService = MealPlanDayService()

    get<Customers> {
        val userDto = call.principal<UserDto>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val isAdmin = userService.isAdmin(userDto.id)
        if (isAdmin == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return@get
        } else if (!isAdmin) {
            call.respond(HttpStatusCode.Forbidden, "Admin access required")
            return@get
        }
        val customers = customerService.findAllWithDetails()
        call.respond(customers)
    }

    get<Customers.Profile> {
        val userDto = call.principal<UserDto>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val customer = customerService.findById(userDto.id)
        if (customer == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return@get
        }
        call.respond(customer)
    }

    put<Customers.Profile> {
        val userDto = call.principal<UserDto>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@put
        }

        val req = call.receive<CustomerUpdateDto>()
        // Simple validation: if provided, they must not be blank strings
        val cleanedFirst = req.firstName?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedLast = req.lastName?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedAddr = req.address?.trim()?.takeIf { it.isNotEmpty() }

        val customer = customerService.update(userDto.id, cleanedFirst, cleanedLast, cleanedAddr)
        if (customer == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
            return@put
        }
        call.respond(customer)
    }

    get<Customers.Plan> {
        val userDto = call.principal<UserDto>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val customerPlan = customerService.getCustomerPlan(userDto.id)
        if (customerPlan == null) {
            call.respond(HttpStatusCode.NotFound, "No plan found for customer")
        } else {
            call.respond(customerPlan)
        }
    }

    post<Customers.Plan> {
        val userDto = call.principal<UserDto>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@post
        }
        val body = call.receive<CustomerPlanCreateDto>()
        try {
            val saved = customerService.saveCustomerPlan(userDto.id, body)
            call.respond(HttpStatusCode.Created, saved)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
        }
    }

    get<Customers.Week> {
        val userDto = call.principal<UserDto>() ?: run {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val all = mpdService.getAll()
        // Determine starting MPD: current or first by serial
        val start = mpdService.getCurrent() ?: all.firstOrNull()

        val today = today()
        val result = mutableListOf<WeeklyPlanDayDto>()
        if (all.isEmpty()) {
            // Return 7 dates with empty dishes if there are no meal plan days
            repeat(7) { i ->
                result.add(WeeklyPlanDayDto(date = today.plus(i, DateTimeUnit.DAY), dishes = emptyList()))
            }
            call.respond(result)
            return@get
        }
        val ordered = all.sortedBy { it.serial }
        val startIndex = start
            ?.let { s -> ordered.indexOfFirst { it.id == s.id } }
            .takeIf { it != null && it >= 0 }
            ?: 0

        var idx = startIndex
        repeat(7) { i ->
            val mpd = ordered[idx]
            val dishes = mpdService.getDishes(mpd.id).getOrElse { emptyList() }
            result.add(WeeklyPlanDayDto(date = today.plus(i, DateTimeUnit.DAY), dishes = dishes))
            idx = (idx + 1) % ordered.size
        }
        call.respond(result)
    }
}
