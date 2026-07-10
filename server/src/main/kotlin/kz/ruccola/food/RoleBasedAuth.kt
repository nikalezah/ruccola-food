package kz.ruccola.food

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext
import kz.ruccola.food.api.Role
import kz.ruccola.food.api.UserDto

class RoleBasedAuthConfig {
    var roles: Set<Role> = emptySet()
}

val RoleBasedAuthPlugin =
    createRouteScopedPlugin(name = "RoleBasedAuth", createConfiguration = ::RoleBasedAuthConfig) {
        val roles = pluginConfig.roles

        on(AuthenticationChecked) { call ->
            val user = call.principal<UserDto>() ?: return@on
            if (user.role !in roles) {
                call.respond(HttpStatusCode.Forbidden, "You do not have permission to access this resource")
            }
        }
    }

fun Route.withRole(vararg roles: Role, build: Route.() -> Unit): Route {
    val authorizedRoute =
        createChild(
            object : RouteSelector() {
                override suspend fun evaluate(
                    context: RoutingResolveContext,
                    segmentIndex: Int,
                ): RouteSelectorEvaluation = RouteSelectorEvaluation.Constant
            }
        )

    authorizedRoute.install(RoleBasedAuthPlugin) { this.roles = roles.toSet() }

    authorizedRoute.build()
    return authorizedRoute
}
