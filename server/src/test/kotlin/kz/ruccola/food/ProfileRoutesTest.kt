package kz.ruccola.food

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.api.Role
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.Users
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRoutesTest {
    @BeforeTest
    fun setup() {
        initializeTestDatabase()
    }

    @Test
    fun testGetProfileWithValidToken() =
        testApplication {
            application { module() }
            // Login as admin to get token
            suspendTransaction {
                val userId = Users.insertAndGetId {
                    it[Users.email] = "customer1@interna.food"
                    it[Users.password] = "123qwe"
                    it[Users.firstName] = "Customer"
                    it[Users.lastName] = "One"
                    it[Users.role] = Role.CUSTOMER
                }.value
                Customers.insert {
                    it[Customers.id] = userId
                    it[Customers.address] = "Address"
                }
            }
            val loginResp = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email":"customer1@interna.food","password":"123qwe"}""")
            }
            assertEquals(HttpStatusCode.OK, loginResp.status)
            val loginJson = Json.parseToJsonElement(loginResp.bodyAsText()).jsonObject
            val token = loginJson["token"]!!.jsonPrimitive.content
            // Call profile
            val profResp = client.get("/api/customers/profile") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, profResp.status)
            val profJson = Json.parseToJsonElement(profResp.bodyAsText()).jsonObject
            assertEquals("customer1@interna.food", profJson["email"]!!.jsonPrimitive.content)
            assertEquals("CUSTOMER", profJson["role"]!!.jsonPrimitive.content)
        }

    @Test
    fun testGetProfileUnauthorized() =
        testApplication {
            application { module() }
            val respNoHeader = client.get("/api/customers/profile")
            assertEquals(HttpStatusCode.Unauthorized, respNoHeader.status)

            val respBadHeader = client.get("/api/customers/profile") {
                header(HttpHeaders.Authorization, "Bearer invalid-token")
            }
            assertEquals(HttpStatusCode.Unauthorized, respBadHeader.status)
        }

    @Test
    fun testLogoutEndpoint() =
        testApplication {
            application { module() }
            val resp = client.post("/api/auth/logout")
            assertEquals(HttpStatusCode.OK, resp.status)
        }
}
