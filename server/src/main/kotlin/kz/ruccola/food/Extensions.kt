package kz.ruccola.food

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kz.ruccola.food.api.PagingResponse
import kz.ruccola.food.api.UserDto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Clock

val ApplicationCall.user: UserDto
    get() = principal<UserDto>() ?: error("User principal not found.")

fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

suspend fun <T> dbQuery(block: suspend () -> T): T = suspendTransaction { block() }

suspend fun <T> Query.toPagingResponse(
    page: Int,
    size: Int,
    transform: suspend (ResultRow) -> T,
): PagingResponse<T> {
    val totalCount = this.count()
    val items = this.copy()
        .limit(size)
        .offset((page * size).toLong())
        .toList()
        .map { transform(it) }

    return PagingResponse(
        items = items,
        totalCount = totalCount,
        page = page,
        size = size,
    )
}
