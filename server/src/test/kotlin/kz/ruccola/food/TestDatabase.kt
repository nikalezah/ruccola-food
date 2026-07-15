package kz.ruccola.food

import kotlinx.coroutines.runBlocking
import kz.ruccola.food.database.DatabaseMigration
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

private const val PRIMARY_DB = "food"
private const val TEMPLATE_DB = "food_tpl"
private const val RUN_DB = "food_run"

object PostgresTestContainer {
    private val container: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer(DockerImageName.parse("postgres:18"))
            .withDatabaseName(PRIMARY_DB)
            .withUsername("food")
            .withPassword("food")
            .also {
                it.start()
                Runtime.getRuntime().addShutdownHook(Thread { it.stop() })
            }
    }

    fun ensureStarted(): PostgreSQLContainer<*> = container

    fun r2dbcUrl(database: String): String {
        val c = ensureStarted()
        return "r2dbc:postgresql://${c.host}:${c.getMappedPort(5432)}/$database"
    }

    fun adminJdbcUrl(database: String = "postgres"): String {
        val c = ensureStarted()
        return "jdbc:postgresql://${c.host}:${c.getMappedPort(5432)}/$database"
    }
}

private var r2dbcDatabase: R2dbcDatabase? = null
private var templateInitialized = false

fun resetTestDatabase() {
    if (!templateInitialized) {
        initializeTemplate()
    }
    disconnectR2dbc()
    adminExecute("DROP DATABASE IF EXISTS $RUN_DB WITH (FORCE)")
    adminExecute("CREATE DATABASE $RUN_DB WITH TEMPLATE $TEMPLATE_DB")
    connectR2dbc(RUN_DB)
}

private fun initializeTemplate() {
    connectR2dbc(PRIMARY_DB)
    runBlocking { DatabaseMigration.migrate(loadSeedData = false) }
    disconnectR2dbc()
    adminExecute("DROP DATABASE IF EXISTS $TEMPLATE_DB WITH (FORCE)")
    adminExecute("CREATE DATABASE $TEMPLATE_DB WITH TEMPLATE $PRIMARY_DB")
    templateInitialized = true
}

private fun connectR2dbc(database: String) {
    disconnectR2dbc()
    r2dbcDatabase =
        R2dbcDatabase.connect(
            url = PostgresTestContainer.r2dbcUrl(database),
            driver = "postgresql",
            user = PostgresTestContainer.ensureStarted().username,
            password = PostgresTestContainer.ensureStarted().password,
        )
}

private fun disconnectR2dbc() {
    r2dbcDatabase?.let { TransactionManager.closeAndUnregister(it) }
    r2dbcDatabase = null
}

private fun adminExecute(sql: String) {
    val c = PostgresTestContainer.ensureStarted()
    DriverManager.getConnection(PostgresTestContainer.adminJdbcUrl(), c.username, c.password).use { connection ->
        connection.createStatement().execute(sql)
    }
}
