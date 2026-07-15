package kz.ruccola.food.database.migration

import kz.ruccola.food.api.Role
import kz.ruccola.food.database.Migration
import kz.ruccola.food.database.execSqlResource
import kz.ruccola.food.localization.Language
import kz.ruccola.food.model.Chats
import kz.ruccola.food.model.CustomerPlans
import kz.ruccola.food.model.Customers
import kz.ruccola.food.model.DayDishes
import kz.ruccola.food.model.Days
import kz.ruccola.food.model.DishImages
import kz.ruccola.food.model.DishTranslations
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

class M001InitialMigration(private val loadSeedData: Boolean = true) : Migration {
    override val version = "001"
    override val description = "Initial migration"

    override suspend fun up() {
        suspendTransaction {
            // Order matters because of FKs. Create base tables first.
            SchemaUtils.create(Users, Dishes, Files, Days, MealPlanDays, Plans)
            // Then create tables that reference the above
            SchemaUtils.create(
                Customers,
                CustomerPlans,
                DayDishes,
                DishImages,
                DishTranslations,
                MealPlanDayDishes,
                Chats,
                Messages,
                MessageReads,
            )

            exec("CREATE UNIQUE INDEX ux_plans_calories_period_days ON plans(calories, period_days);")
            // Unique partial index: only one meal plan a day may have current = TRUE
            exec("CREATE UNIQUE INDEX ux_meal_plan_days_current_true ON meal_plan_days(current) WHERE current = TRUE;")
            val languages = Language.entries.joinToString(", ") { "'${it.name}'" }
            exec(
                """
                ALTER TABLE dish_translations ADD CONSTRAINT chk_dish_translations_language
                CHECK (language IN ($languages));
                """
                    .trimIndent()
            )
            exec(
                """
                ALTER TABLE dish_translations ADD CONSTRAINT chk_dish_translations_name_format
                CHECK (
                  (language = '${Language.EN.name}' AND name ~ '${Language.EN.dishNamePattern}') OR
                  (language = '${Language.RU.name}' AND name ~ '${Language.RU.dishNamePattern}') OR
                  (language = '${Language.KK.name}' AND name ~ '${Language.KK.dishNamePattern}')
                );
                """
                    .trimIndent()
            )

            println("[DEBUG_LOG] Created core tables and relations")

            // Seed default admin user
            Users.insert {
                it[Users.email] = "admin@ruccola.test"
                it[Users.password] = "123qwe"
                it[Users.firstName] = "Admin"
                it[Users.lastName] = "Admin"
                it[Users.role] = Role.ADMIN
            }

            if (loadSeedData) {
                execSqlResource("/db/init_inserts.sql")
                execSqlResource("/db/test_users.sql")
                println("[DEBUG_LOG] Loaded init_inserts.sql and test_users.sql")
            }
        }
    }
}
