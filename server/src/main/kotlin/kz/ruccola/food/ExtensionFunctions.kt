package kz.ruccola.food

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Clock

fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

suspend fun <T> dbQuery(block: suspend () -> T): T = suspendTransaction { block() }
