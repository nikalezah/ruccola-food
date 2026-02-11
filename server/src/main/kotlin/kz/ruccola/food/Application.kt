package kz.ruccola.food

import io.ktor.server.application.Application
import io.ktor.server.cio.EngineMain
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kz.ruccola.food.database.DatabaseMigration
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}

@Suppress("unused") // used in application.conf
fun Application.configureDatabase() {
    val url = environment.config.property("ktor.db.url").getString()
    val user = environment.config.property("ktor.db.user").getString()
    val password = environment.config.property("ktor.db.password").getString()
    R2dbcDatabase.connect(
        url = url,
        driver = "postgresql",
        user = user,
        password = password,
    )
    runBlocking {
        DatabaseMigration.migrate()
    }
}

fun initializeTestDatabase() {
    R2dbcDatabase.connect(
        url = "r2dbc:postgresql://localhost:5433/food",
        driver = "postgresql",
        user = "food",
        password = "food",
    )
    runBlocking {
        suspendTransaction {
            exec("DROP SCHEMA IF EXISTS public CASCADE;")
            exec("CREATE SCHEMA public;")
        }
        DatabaseMigration.migrate()
    }
}
