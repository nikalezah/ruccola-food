package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.api.Auth
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.api.LoginRequestDto
import kz.ruccola.food.api.RegisterRequestDto
import kz.ruccola.food.service.UserService

fun Route.configureAuthRoutes() {
    val userService = UserService()

    post<Auth.Register> {
        val req = call.receive<RegisterRequestDto>()
        if (req.password != req.confirmPassword) {
            call.respond(HttpStatusCode.BadRequest, "Passwords do not match")
            return@post
        }
        if (req.address.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Address is required")
            return@post
        }
        if (userService.existsByEmail(req.email)) {
            call.respond(HttpStatusCode.Conflict, "Email already registered")
            return@post
        }
        val created = userService.createUser(
            email = req.email,
            password = req.password,
            firstName = req.firstName,
            lastName = req.lastName,
            address = req.address,
        )
        val token = "dummy-token-${created.id}"
        call.respond(HttpStatusCode.Created, AuthResponseDto(token = token, user = created))
    }

    post<Auth.Login> {
        val req = call.receive<LoginRequestDto>()
        val user = userService.findByEmail(req.email)
        if (user == null || user.second != req.password) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
            return@post
        }
        val token = "dummy-token-${user.first.id}"
        call.respond(AuthResponseDto(token = token, user = user.first))
    }

    post<Auth.Logout> {
        call.respond(HttpStatusCode.OK)
    }
}
