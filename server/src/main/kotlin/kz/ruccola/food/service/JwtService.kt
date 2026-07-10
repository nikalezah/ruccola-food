package kz.ruccola.food.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date

class JwtService(config: ApplicationConfig) {
    private val secret = config.property("ktor.jwt.secret").getString()
    private val issuer = config.property("ktor.jwt.domain").getString()
    private val audience = config.property("ktor.jwt.audience").getString()
    private val realm = config.property("ktor.jwt.realm").getString()

    fun generateToken(userId: Int): String =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 36_00_000 * 24)) // 24 hours
            .sign(Algorithm.HMAC256(secret))

    fun getVerifier() = JWT.require(Algorithm.HMAC256(secret)).withAudience(audience).withIssuer(issuer).build()

    fun getRealm(): String = realm
}
