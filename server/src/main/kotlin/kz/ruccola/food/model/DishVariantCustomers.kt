package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object DishVariantCustomers : Table("dish_variant_customers") {
    val variantId = reference("variant_id", DishVariants, onDelete = ReferenceOption.CASCADE)
    val customerId = reference("customer_id", Customers, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(variantId, customerId, name = "pk_dish_variant_customers")
}
