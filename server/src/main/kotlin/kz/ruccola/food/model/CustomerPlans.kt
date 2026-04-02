package kz.ruccola.food.model

import kz.ruccola.food.today
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.date

object CustomerPlans : IntIdTable("customer_plans") {
    val customer = reference("customer_id", Customers, onDelete = ReferenceOption.CASCADE)
    val plan = reference("plan_id", Plans, onDelete = ReferenceOption.RESTRICT)
    val calories = integer("calories")
    val pricePerDay = integer("price_per_day")
    val days = integer("days")
    val chosenDate = date("chosen_date").clientDefault { today() }

    init {
        uniqueIndex(customer, plan, chosenDate)
    }
}
