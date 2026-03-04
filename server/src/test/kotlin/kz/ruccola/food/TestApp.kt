package kz.ruccola.food

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

fun testApp(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) =
    testApplication {
        environment { config = ApplicationConfig("application-test.conf") }
        application { module() }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        block(client)
    }
