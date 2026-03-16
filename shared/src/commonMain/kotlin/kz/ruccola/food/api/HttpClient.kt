package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kz.ruccola.food.BASE_URL

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }
    install(Resources)
    install(DefaultRequest) {
        url("$BASE_URL/api/")
    }
    install(Auth) {
        bearer {
            loadTokens {
                TokenProvider.token?.let { BearerTokens(it, "") }
            }
            sendWithoutRequest {
                TokenProvider.token != null
            }
        }
    }
}

object TokenProvider {
    var token: String? = null
        set(value) {
            field = value
            // Clear the Ktor Auth cache so it calls loadTokens again on the next request
            httpClient.authProviders
                .filterIsInstance<BearerAuthProvider>()
                .forEach { it.clearToken() }
        }
}
