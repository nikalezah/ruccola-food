package kz.ruccola.food

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kz.ruccola.food.api.UserDto
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Clock

fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

suspend fun <T> dbQuery(block: suspend () -> T): T = suspendTransaction { block() }

val ApplicationCall.user: UserDto
    get() = principal<UserDto>() ?: error("User principal not found.")
