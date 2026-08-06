package com.lovelycatv.crystalframework.shared.config.database

import com.lovelycatv.crystalframework.shared.config.SqlStatementInterceptor
import com.lovelycatv.vertex.log.logger
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.update.Update

/**
 * Pure dispatcher for outbound SQL rewriting: it parses the SQL once, resolves the target table,
 * then runs every registered [com.lovelycatv.crystalframework.shared.config.SqlStatementInterceptor] in ascending [com.lovelycatv.crystalframework.shared.config.SqlStatementInterceptor.order].
 * It holds no business rules of its own — soft-delete and `modified_time` maintenance live in
 * [com.lovelycatv.crystalframework.shared.config.SoftDeleteSqlInterceptor], and any module can contribute more interceptors as Spring beans.
 *
 * The interceptor chain is injected at startup (see the wiring in [R2dbcSQLInterceptorConfig]) so
 * this stays a singleton that call sites can invoke statically.
 */
object CrystalFrameworkSQLModifier {
    private val logger = logger()

    @Volatile
    private var interceptors: List<SqlStatementInterceptor> = emptyList()

    /**
     * Installs the interceptor chain, ordered by [SqlStatementInterceptor.order]. Called once during
     * context startup; the highest-precedence interceptor runs first.
     */
    fun setInterceptors(interceptors: Collection<SqlStatementInterceptor>) {
        this.interceptors = interceptors.sortedBy { it.order }
    }

    fun processSql(p0: String): String {
        if (interceptors.isEmpty()) {
            return p0
        }

        val statement = try {
            CCJSqlParserUtil.parse(p0)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while parsing SQL from $p0", e)
        }

        val tableName = extractTargetTableName(statement)

        var current = statement
        for (interceptor in interceptors) {
            current = interceptor.intercept(current, tableName)
        }

        val sql = current.toString()
        if (sql != p0) {
            logger.debug("Modified SQL:")
            logger.debug("        from: $p0")
            logger.debug("          to: $sql")
        }

        return sql
    }

    private fun extractTargetTableName(statement: Statement): String? {
        fun stripQuotes(name: String): String {
            return name.removeSurrounding("\"").removeSurrounding("`")
        }
        return when (statement) {
            is PlainSelect -> {
                val fromItem = statement.fromItem
                if (fromItem is Table) stripQuotes(fromItem.name).lowercase() else null
            }
            is Insert -> stripQuotes(statement.table.name).lowercase()
            is Update -> stripQuotes(statement.table.name).lowercase()
            is Delete -> stripQuotes(statement.table.name).lowercase()
            else -> null
        }
    }
}