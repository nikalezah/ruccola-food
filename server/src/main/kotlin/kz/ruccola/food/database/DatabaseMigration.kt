package kz.ruccola.food.database

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kz.ruccola.food.database.migration.M001InitialMigration
import kz.ruccola.food.now
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object DatabaseMigration {
    // Made public so it can be accessed for database clearing
    object MigrationHistory : Table("migration_history") {
        val id = integer("id").autoIncrement()
        val version = varchar("version", 50)
        val description = varchar("description", 200)
        val appliedAt = varchar("applied_at", 50)

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun migrate() {
        suspendTransaction {
            // Create a migration history table if it doesn't exist
            SchemaUtils.create(MigrationHistory)

            // Get applied migrations
            val appliedVersions = MigrationHistory.selectAll()
                .map { it[MigrationHistory.version] }
                .toSet()

            // List of migration classes (add new ones here)
            val migrations = listOf(
                M001InitialMigration(),
                // ... add future migrations here
            )

            // Apply each migration if not already applied
            for (migration in migrations) {
                if (migration.version in appliedVersions) {
                    println("Migration ${migration.version} already applied, skipping")
                    continue
                }

                println("Applying migration ${migration.version}: ${migration.description}")
                try {
                    migration.up()

                    // Record migration in history only if successful
                    MigrationHistory.insert {
                        it[version] = migration.version
                        it[description] = migration.description
                        it[appliedAt] = now().toString()
                    }

                    println("Migration ${migration.version} applied successfully")
                } catch (e: Exception) {
                    // Log the actual error and stop
                    println("ERROR: Migration ${migration.version} failed: ${e.message}")
                    e.printStackTrace()
                    // Re-throw to stop the application startup
                    throw e
                }
            }
        }
    }
}

interface Migration {
    val version: String
    val description: String

    suspend fun up()
}
