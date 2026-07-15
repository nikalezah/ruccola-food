package kz.ruccola.food.database

import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

suspend fun R2dbcTransaction.execSqlResource(resourcePath: String) {
    val stream =
        DatabaseMigration::class.java.getResourceAsStream(resourcePath)
            ?: error("SQL resource not found: $resourcePath")
    val sql = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    for (statement in splitSqlStatements(sql)) {
        exec(statement)
    }
}

/**
 * Splits SQL on ';' outside single-quoted string literals. Line comments starting with '--' are stripped before
 * splitting.
 */
internal fun splitSqlStatements(sql: String): List<String> {
    val withoutComments =
        sql.lineSequence()
            .map { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("--")) "" else line
            }
            .joinToString("\n")

    val statements = mutableListOf<String>()
    val current = StringBuilder()
    var inString = false
    var i = 0
    while (i < withoutComments.length) {
        val c = withoutComments[i]
        when {
            c == '\'' -> {
                current.append(c)
                if (inString && i + 1 < withoutComments.length && withoutComments[i + 1] == '\'') {
                    // Escaped quote ''
                    current.append('\'')
                    i++
                } else {
                    inString = !inString
                }
            }

            c == ';' && !inString -> {
                val stmt = current.toString().trim()
                if (stmt.isNotEmpty()) statements += stmt
                current.clear()
            }

            else -> current.append(c)
        }
        i++
    }
    val tail = current.toString().trim()
    if (tail.isNotEmpty()) statements += tail
    return statements
}
