package kz.ruccola.food

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kz.ruccola.food.database.DatabaseMigration
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

private const val PRIMARY_DB = "food"
private const val TEMPLATE_DB = "food_tpl"
private const val RUN_DB = "food_run"
private const val ADMIN_DB = "postgres"

object PostgresTestContainer {
    private val container: PostgreSQLContainer by lazy {
        PostgreSQLContainer(DockerImageName.parse("postgres:18"))
            .withDatabaseName(PRIMARY_DB)
            .withUsername("food")
            .withPassword("food")
            .also {
                it.start()
                Runtime.getRuntime().addShutdownHook(Thread { it.stop() })
            }
    }

    fun ensureStarted(): PostgreSQLContainer = container

    fun r2dbcUrl(database: String): String {
        val c = ensureStarted()
        return "r2dbc:postgresql://${c.host}:${c.getMappedPort(5432)}/$database"
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

private fun adminExecute(sql: String) =
    runBlocking {
        val c = PostgresTestContainer.ensureStarted()
        val connectionFactory =
            ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                    .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                    .option(ConnectionFactoryOptions.HOST, c.host)
                    .option(ConnectionFactoryOptions.PORT, c.getMappedPort(5432))
                    .option(ConnectionFactoryOptions.USER, c.username)
                    .option(ConnectionFactoryOptions.PASSWORD, c.password)
                    .option(ConnectionFactoryOptions.DATABASE, ADMIN_DB)
                    .build(),
            )
        val connection = connectionFactory.create().awaitSingle()
        try {
            val result = connection.createStatement(sql).execute().awaitSingle()
            result.rowsUpdated.awaitFirstOrNull()
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }
