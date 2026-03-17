package kz.ruccola.food.route

import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kz.ruccola.food.api.Days
import kz.ruccola.food.api.Role
import kz.ruccola.food.runMidnightFor
import kz.ruccola.food.service.DayService
import kz.ruccola.food.today
import kz.ruccola.food.withRole

fun Route.configureDayRoutes() {
    val dayService = DayService()

    withRole(Role.ADMIN) {
        get<Days> {
            val days = dayService.getAllDays()
            call.respond(days)
        }

        post<Days.Midnight> {
            // Always compute the next date after the maximum existing Day (or today if none)
            val effectiveDate = dayService.getLatestDate()?.plus(1, DateTimeUnit.DAY) ?: today()
            // trigger midnight logic for effectiveDate
            runMidnightFor(effectiveDate)
            call.respondText("ok; currentDate=$effectiveDate")
        }
    }
}
