package kz.ruccola.food.service

import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kz.ruccola.food.api.Role
import kz.ruccola.food.api.UserDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.selectAll

class UserService {
    suspend fun findById(id: Int): UserDto? =
        dbQuery {
            Users.selectAll().where { Users.id eq id }
                .singleOrNull()
                ?.let { toDto(it) }
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

    // todo: separate creation of customer and user
    suspend fun createUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        address: String,
    ): UserDto =
        dbQuery {
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
        UserDto(row[Users.id].value, row[Users.email], row[Users.firstName], row[Users.lastName], row[Users.role])
}
