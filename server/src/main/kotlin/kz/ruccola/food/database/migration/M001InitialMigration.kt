package kz.ruccola.food.database.migration

import kz.ruccola.food.api.Role
import kz.ruccola.food.database.Migration
import kz.ruccola.food.model.Chats
import kz.ruccola.food.model.CustomerPlans
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.DayDishes
import kz.ruccola.food.model.Days
import kz.ruccola.food.model.DishImages
import kz.ruccola.food.model.DishVariantCustomers
import kz.ruccola.food.model.DishVariants
import kz.ruccola.food.model.Dishes
import kz.ruccola.food.model.Files
import kz.ruccola.food.model.MealPlanDayDishes
import kz.ruccola.food.model.MealPlanDays
import kz.ruccola.food.model.MessageReads
import kz.ruccola.food.model.Messages
import kz.ruccola.food.model.Plans
import kz.ruccola.food.model.Users
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class M001InitialMigration : Migration {
    override val version = "001"
    override val description = "Initial migration"

    override suspend fun up() {
        suspendTransaction {
            // Order matters because of FKs. Create base tables first.
            SchemaUtils.create(
                Users,
                Dishes,
                Files,
                Days,
                MealPlanDays,
                Plans,
            )
            // Then create tables that reference the above
            SchemaUtils.create(
                Customers,
                CustomerPlans,
                DayDishes,
                DishImages,
                DishVariants,
                DishVariantCustomers,
                MealPlanDayDishes,
                Chats,
                Messages,
                MessageReads,
            )

            // Unique partial index: only one meal plan a day may have current = TRUE
            exec("CREATE UNIQUE INDEX ux_meal_plan_days_current_true ON meal_plan_days(current) WHERE current = TRUE;")

            println("[DEBUG_LOG] Created core tables and relations")

            // Seed default admin user
            Users.insert {
                it[Users.email] = "admin@interna.food"
                it[Users.password] = "admin"
                it[Users.firstName] = "Admin"
                it[Users.lastName] = "Admin"
                it[Users.role] = Role.ADMIN
            }
        }
    }
}
