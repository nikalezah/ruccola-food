package kz.ruccola.food.service

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kz.ruccola.food.api.CustomerDto
import kz.ruccola.food.api.CustomerPlanCreateDto
import kz.ruccola.food.api.CustomerPlanDetailsDto
import kz.ruccola.food.api.CustomerPrefsDto
import kz.ruccola.food.api.CustomerPrefsUpdateDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.Chats
import kz.ruccola.food.model.CustomerPlans
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.Messages
import kz.ruccola.food.model.Plans
import kz.ruccola.food.model.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

class CustomerService {
    suspend fun findById(id: Int): CustomerDto? =
        dbQuery {
            Customers.innerJoin(Users).selectAll()
                .where { Customers.id eq id }
                .singleOrNull()
                ?.let(::toDto)
        }

    suspend fun findAllWithDetails(): List<CustomerDto> =
        dbQuery {
            val customers = Customers.innerJoin(Users).selectAll().toList()
            val customerIds = customers.map { it[Customers.id].value }

            if (customerIds.isEmpty()) return@dbQuery emptyList()

            val latestPlanCalories = CustomerPlans.innerJoin(Plans).selectAll()
                .where { CustomerPlans.customer inList customerIds }
                .orderBy(CustomerPlans.customer to SortOrder.ASC, CustomerPlans.chosenDate to SortOrder.DESC)
                .toList()
                .groupBy { it[CustomerPlans.customer].value }
                .mapValues { (_, rows) -> rows.first()[Plans.calories] }

            val lastMessages = Messages.innerJoin(Chats).select(Messages.body, Chats.customerId)
                .where { Chats.customerId inList customerIds }
                .orderBy(Chats.customerId to SortOrder.ASC, Messages.id to SortOrder.DESC)
                .toList()
                .groupBy { it[Chats.customerId].value }
                .mapValues { (_, rows) -> rows.first()[Messages.body] }

            customers.map { row ->
                val id = row[Customers.id].value
                toDto(row, latestPlanCalories[id], lastMessages[id])
            }
        }

    suspend fun update(
        id: Int,
        firstName: String?,
        lastName: String?,
        address: String?,
    ): CustomerDto? =
        dbQuery {
            val updatedUsers = if (firstName != null || lastName != null) {
                Users.update({ Users.id eq id }) {
                    firstName?.let { n -> it[Users.firstName] = n }
                    lastName?.let { n -> it[Users.lastName] = n }
                }
            } else {
                0
            }

            val updatedCustomers = address?.let { addr ->
                Customers.update({ Customers.id eq id }) {
                    it[Customers.address] = addr
                }
            } ?: 0

            if (updatedUsers == 0 && updatedCustomers == 0) {
                null
            } else {
                Customers.innerJoin(Users).selectAll()
                    .where { Customers.id eq id }
                    .singleOrNull()
                    ?.let(::toDto)
            }
        }

    suspend fun getCustomerPlan(customerId: Int): CustomerPlanDetailsDto? =
        dbQuery {
            val cp = CustomerPlans.selectAll()
                .where { CustomerPlans.customer eq customerId }
                .orderBy(CustomerPlans.chosenDate to SortOrder.DESC)
                .firstOrNull()
            if (cp == null) null else toCustomerPlanDetailsDto(cp)
        }

    suspend fun saveCustomerPlan(
        customerId: Int,
        newCustomerPlan: CustomerPlanCreateDto,
    ): CustomerPlanDetailsDto =
        dbQuery {
            Customers.selectAll().where { Customers.id eq customerId }.singleOrNull()
                ?: throw IllegalArgumentException("Customer not found")

            val plan = Plans.selectAll()
                .where { Plans.id eq newCustomerPlan.planId }
                .singleOrNull()
                ?: throw IllegalArgumentException("Plan not found")

            val calories = plan[Plans.calories]

            val thresholdPlan = Plans.selectAll()
                .where { (Plans.calories eq calories) and (Plans.periodDays lessEq newCustomerPlan.days) }
                .orderBy(Plans.periodDays to SortOrder.DESC)
                .firstOrNull()
                ?: throw IllegalArgumentException("No plan available for the requested number of days")

            val thresholdPricePerDay = thresholdPlan[Plans.pricePerDay]
            val thresholdPlanId = thresholdPlan[Plans.id]

            CustomerPlans.deleteWhere {
                (CustomerPlans.customer eq customerId) and (CustomerPlans.chosenDate eq newCustomerPlan.chosenDate)
            }
            val cp = CustomerPlans.insertReturning {
                it[CustomerPlans.customer] = customerId
                it[CustomerPlans.plan] = thresholdPlanId
                it[CustomerPlans.chosenDate] = newCustomerPlan.chosenDate
                it[CustomerPlans.calories] = calories
                it[CustomerPlans.pricePerDay] = thresholdPricePerDay
                it[CustomerPlans.days] = newCustomerPlan.days
            }.single()
            toCustomerPlanDetailsDto(cp)
        }

    suspend fun updateCustomerPrefs(
        id: Int,
        prefs: CustomerPrefsUpdateDto,
    ): CustomerPrefsDto? =
        dbQuery {
            val updateCount = Customers.update({ Customers.id eq id }) {
                prefs.needsCutlery?.let { v -> it[needsCutlery] = v }
                prefs.weekendDelivery?.let { v -> it[weekendDelivery] = v }
                prefs.morningDelivery?.let { v -> it[morningDelivery] = v }
            }
            if (updateCount == 0) return@dbQuery null
            Customers.selectAll()
                .where { Customers.id eq id }
                .singleOrNull()
                ?.let(::toCustomerPrefsDto)
        }

    fun toDto(
        row: ResultRow,
        calories: Int? = null,
        lastMessage: String? = null,
    ): CustomerDto =
        CustomerDto(
            row[Customers.id].value,
            row[Users.email],
            row[Users.firstName],
            row[Users.lastName],
            row[Customers.address],
            row[Users.role].name,
            CustomerPrefsDto(
                row[Customers.needsCutlery],
                row[Customers.weekendDelivery],
                row[Customers.morningDelivery],
            ),
            calories,
            lastMessage,
        )

    fun toCustomerPrefsDto(row: ResultRow): CustomerPrefsDto =
        CustomerPrefsDto(
            needsCutlery = row[Customers.needsCutlery],
            weekendDelivery = row[Customers.weekendDelivery],
            morningDelivery = row[Customers.morningDelivery],
        )

    fun toCustomerPlanDetailsDto(row: ResultRow): CustomerPlanDetailsDto =
        CustomerPlanDetailsDto(
            row[CustomerPlans.id].value,
            row[CustomerPlans.customer].value,
            row[CustomerPlans.calories],
            row[CustomerPlans.pricePerDay],
            row[CustomerPlans.days],
            row[CustomerPlans.chosenDate],
        )
}
