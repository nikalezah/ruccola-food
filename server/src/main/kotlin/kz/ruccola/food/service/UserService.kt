package kz.ruccola.food.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kz.ruccola.food.api.Role
import kz.ruccola.food.api.UserDto
import kz.ruccola.food.api.UserWithPasswordDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

class UserService {
    suspend fun findAll(): List<UserWithPasswordDto> =
        dbQuery {
            Users.selectAll().map(::toUserWithPasswordDto).toList()
        }

    suspend fun isAdmin(id: Int): Boolean? =
        dbQuery {
            Users.select(Users.role).where { Users.id eq id }
                .singleOrNull()
                ?.get(Users.role)
                ?.let { it == Role.ADMIN }
        }

    suspend fun findByEmail(email: String): Pair<UserDto, String>? =
        dbQuery {
            Users.selectAll().where { Users.email eq email }
                .singleOrNull()
                ?.let { toDto(it) to it[Users.password] } // todo: remove, don't return password
        }

    suspend fun existsByEmail(email: String): Boolean =
        dbQuery {
            Users.selectAll().where { Users.email eq email }.count() > 0
        }

    suspend fun createUser( // todo: separate creation of customer and user
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        address: String,
    ): UserDto = dbQuery {
        val user = Users.insertReturning {
            it[Users.email] = email
            it[Users.password] = password
            it[Users.firstName] = firstName
            it[Users.lastName] = lastName
            it[Users.role] = Role.CUSTOMER
        }.single()
        Customers.insert {
            it[Customers.id] = user[Users.id]
            it[Customers.address] = address
        }
        toDto(user)
    }

    fun toDto(row: ResultRow): UserDto =
        UserDto(row[Users.id].value, row[Users.email], row[Users.firstName], row[Users.lastName], row[Users.role].name)

    fun toUserWithPasswordDto(row: ResultRow): UserWithPasswordDto =
        UserWithPasswordDto(
            row[Users.id].value,
            row[Users.email],
            row[Users.firstName],
            row[Users.lastName],
            runBlocking {
                // todo: join customer address to avoid n+1 additional query
                Customers.selectAll().where {
                    Customers.id eq row[Users.id].value
                }.singleOrNull()?.get(Customers.address)
            },
            row[Users.role].name,
            row[Users.password],
        )
}
