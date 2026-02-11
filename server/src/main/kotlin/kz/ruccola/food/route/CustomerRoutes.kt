package kz.ruccola.food.route

import io.ktor.http.HttpHeaders
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
import kz.ruccola.food.api.WeeklyPlanDayDto
import kz.ruccola.food.service.CustomerService
import kz.ruccola.food.service.MealPlanDayService
import kz.ruccola.food.service.UserService
import kz.ruccola.food.today

fun Route.configureCustomerRoutes() {
    val userService = UserService()
    val customerService = CustomerService()
    val mpdService = MealPlanDayService()

    get<Customers> {
        // Authorization header check similar to /api/profile
        val authHeader = call.request.headers[HttpHeaders.Authorization]
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val token = authHeader.removePrefix("Bearer ").trim()
        val parts = token.split("-")
        val userId = parts.lastOrNull()?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            return@get
        }
        val isAdmin = userService.isAdmin(userId)
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
        val auth = call.request.headers[HttpHeaders.Authorization]
        if (auth == null || !auth.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val token = auth.removePrefix("Bearer ").trim()
        // Expecting format: dummy-token-<id>
        val parts = token.split("-")
        val idPart = parts.lastOrNull()
        val id = idPart?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || id == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            return@get
        }
        val customer = customerService.findById(id)
        if (customer == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return@get
        }
        call.respond(customer)
    }

    put<Customers.Profile> {
        val auth = call.request.headers[HttpHeaders.Authorization]
        if (auth == null || !auth.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@put
        }
        val token = auth.removePrefix("Bearer ").trim()
        val parts = token.split("-")
        val idPart = parts.lastOrNull()
        val id = idPart?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || id == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            return@put
        }

        val req = call.receive<CustomerUpdateDto>()
        // Simple validation: if provided, they must not be blank strings
        val cleanedFirst = req.firstName?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedLast = req.lastName?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedAddr = req.address?.trim()?.takeIf { it.isNotEmpty() }

        val customer = customerService.update(id, cleanedFirst, cleanedLast, cleanedAddr)
        if (customer == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
            return@put
        }
        call.respond(customer)
    }

    get<Customers.Plan> {
        val auth = call.request.headers[HttpHeaders.Authorization]
        if (auth == null || !auth.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val token = auth.removePrefix("Bearer ").trim()
        // Expecting format: dummy-token-<id>
        val parts = token.split("-")
        val idPart = parts.lastOrNull()
        val id = idPart?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || id == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            return@get
        }
        val customerPlan = customerService.getCustomerPlan(id)
        if (customerPlan == null) {
            call.respond(HttpStatusCode.NotFound, "No plan found for customer")
        } else {
            call.respond(customerPlan)
        }
    }

    post<Customers.Plan> {
        val auth = call.request.headers[HttpHeaders.Authorization]
        if (auth == null || !auth.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@post
        }
        val token = auth.removePrefix("Bearer ").trim()
        // Expecting format: dummy-token-<id>
        val parts = token.split("-")
        val idPart = parts.lastOrNull()
        val id = idPart?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || id == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            return@post
        }
        val body = call.receive<CustomerPlanCreateDto>()
        try {
            val saved = customerService.saveCustomerPlan(id, body)
            call.respond(HttpStatusCode.Created, saved)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
        }
    }

    get<Customers.Week> {
        val auth = call.request.headers[HttpHeaders.Authorization]
        if (auth == null || !auth.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid Authorization header")
            return@get
        }
        val token = auth.removePrefix("Bearer ").trim()
        val parts = token.split("-")
        val userId = parts.lastOrNull()?.toIntOrNull()
        if (!token.startsWith("dummy-token-") || userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
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
