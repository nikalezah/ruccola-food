package kz.ruccola.food

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.api.LoginRequestDto
import kz.ruccola.food.api.RegisterRequestDto

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

suspend fun HttpClient.loginAdmin(): String {
    val response = post("/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequestDto("admin@gmail.com", "123qwe"))
    }
    if (response.status != HttpStatusCode.OK) {
        throw IllegalStateException("Failed to login as admin: ${response.status}")
    }
    val body = response.bodyAsText()
    val json = Json.parseToJsonElement(body).jsonObject
    return json["token"]!!.jsonPrimitive.content
}

suspend fun HttpClient.registerCustomer(email: String = uniqueEmail()): AuthResponseDto {
    val response = post("/api/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequestDto(email, "password", "password", "John", "Doe", "123 Main St"))
    }
    if (response.status != HttpStatusCode.Created) {
        throw IllegalStateException("Failed to create customer: ${response.status}")
    }
    return response.body<AuthResponseDto>()
}

fun HttpRequestBuilder.authHeader(token: String) {
    header(HttpHeaders.Authorization, "Bearer $token")
}
