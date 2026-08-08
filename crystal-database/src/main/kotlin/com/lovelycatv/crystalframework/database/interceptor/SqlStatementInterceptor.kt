package com.lovelycatv.crystalframework.database.interceptor

import net.sf.jsqlparser.statement.Statement
import org.springframework.core.Ordered

/**
 * SPI for pluggable SQL rewriting. [com.lovelycatv.crystalframework.database.utils.CrystalFrameworkSQLModifier] parses each outbound SQL once,
 * resolves its target table, then feeds it through every registered interceptor in ascending
 * [order]. Register an implementation as a Spring `@Component` to have it picked up automatically.
 *
 * The dispatcher has already parsed the statement, so an interceptor mutates the [Statement] in
 * place and returns it (or returns the input untouched to opt out). Working on the AST avoids
 * re-parsing and composes cleanly when several interceptors edit the same statement; an interceptor
 * that would rather rewrite text can still do so via `statement.toString()` inside [intercept].
 *
 * The built-in soft-delete / `modified_time` maintenance lives in [SoftDeleteSqlInterceptor], which
 * also owns the BaseEntity-table gating that used to sit in the dispatcher — the dispatcher itself
 * carries no business rules.
 */
interface SqlStatementInterceptor : Ordered {

    /**
     * Rewrite the already-parsed [statement] (whose target table is [tableName], lower-cased, or
     * null when it could not be resolved). Return the same or a new [Statement]; the dispatcher
     * serialises the result back to SQL. Returning [statement] unchanged opts out.
     */
    fun intercept(statement: Statement, tableName: String?): Statement = statement

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}
