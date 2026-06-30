package kz.ruccola.food

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.cio.EngineMain
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kz.ruccola.food.database.DatabaseMigration
import kz.ruccola.food.route.configureAuthRoutes
import kz.ruccola.food.route.configureChatRoutes
import kz.ruccola.food.route.configureCustomerRoutes
import kz.ruccola.food.route.configureDayRoutes
import kz.ruccola.food.route.configureDishRoutes
import kz.ruccola.food.route.configureFileRoutes
import kz.ruccola.food.route.configureMealPlanDayRoutes
import kz.ruccola.food.route.configurePlanRoutes
import kz.ruccola.food.service.DayService
import kz.ruccola.food.service.FileService.Companion.FILES_URL_PREFIX
import kz.ruccola.food.service.JwtService
import kz.ruccola.food.service.MealPlanDayService
import kz.ruccola.food.service.UserService
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import java.io.File

fun main(args: Array<String>): Unit = EngineMain.main(args)

object AppConfig {
    lateinit var config: ApplicationConfig

    val storagePath
        get() = config.property("ktor.storage.path").getString()

    fun init(config: ApplicationConfig) {
        this.config = config
    }
}

fun Application.module() {
    AppConfig.init(environment.config)

    // Install plugins
    install(CallLogging)
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                encodeDefaults = true
            },
        )
    }

    // Configure CORS for browser clients
    install(CORS) {
        anyHost()
        allowNonSimpleContentTypes = true
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(Resources)

    val jwtService = JwtService(environment.config)
    val userService = UserService()

    install(Authentication) {
        jwt {
            realm = jwtService.getRealm()
            verifier(jwtService.getVerifier())
            validate { credential ->
                val id = credential.payload.getClaim("id").asInt()
                if (id != null) {
                    userService.findById(id)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authenticate {
            staticFiles(FILES_URL_PREFIX, File(AppConfig.storagePath))
        }
        route("/api/") {
            configureAuthRoutes(jwtService)
            authenticate {
                configurePlanRoutes()
                configureDishRoutes()
                configureFileRoutes()
                configureDayRoutes()
                configureMealPlanDayRoutes()
                configureCustomerRoutes()
                configureChatRoutes()
            }
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

// todo: run every midnight
suspend fun runMidnightFor(currentDate: LocalDate) {
    val mealPlanService = MealPlanDayService()
    val dayService = DayService()
    val current = mealPlanService.getCurrent() ?: return
    // If Day for the currentDate already exists, do nothing
    val existingCurrent = dayService.findByDate(currentDate)
    if (existingCurrent != null) {
        println("[DEBUG_LOG] Midnight: Day for $currentDate already exists, skipping")
        return
    }
    // Create Day for the currentDate by copying dishes from current MealPlanDay, then advance
    dayService.copyMealPlanDishesToDate(current.id, currentDate)
    mealPlanService.advanceCurrentToNext()
    println("[DEBUG_LOG] Midnight: created Day for $currentDate and advanced current MealPlanDay")
}
