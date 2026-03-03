package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Resource("auth")
class Auth {
    @Resource("register")
    class Register(
        val parent: Auth = Auth(),
    )

    @Resource("login")
    class Login(
        val parent: Auth = Auth(),
    )

    @Resource("logout")
    class Logout(
        val parent: Auth = Auth(),
    )
}

class AuthApi(
    private val client: HttpClient = httpClient,
) {
    suspend fun register(req: RegisterRequestDto): AuthResponseDto {
        val response = client.post(Auth.Register()) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun login(
        email: String,
        password: String,
    ): AuthResponseDto {
        val response = client.post(Auth.Login()) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }
        if (!response.status.isSuccess()) {
            val msg = if (response.status == HttpStatusCode.Unauthorized) {
                "Invalid email or password"
            } else {
                runCatching { response.bodyAsText() }.getOrNull()?.ifBlank { null }
                    ?: "HTTP ${response.status.value}"
            }
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun logout(token: String) {
        val response = client.post(Auth.Logout()) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (!response.status.isSuccess()) {
            throw Exception("HTTP ${response.status.value}")
        }
    }
}

enum class Role { ADMIN, CUSTOMER }

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val address: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)
