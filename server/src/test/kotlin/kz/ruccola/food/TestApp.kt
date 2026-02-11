package kz.ruccola.food

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

fun testApp(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) =
    testApplication {
        application { module() }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        block(client)
    }
