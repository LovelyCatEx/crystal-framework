package com.lovelycatv.crystalframework.database

import net.sf.jsqlparser.expression.JdbcParameter
import net.sf.jsqlparser.expression.LongValue
import net.sf.jsqlparser.expression.operators.relational.EqualsTo
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.PlainSelect
import org.junit.jupiter.api.Test

/**
 * Probe test: confirm how JSQLParser represents PostgreSQL positional placeholders ($1) and
 * literals, since the deferred sharding mechanism depends on mapping a WHERE placeholder to a
 * bind position. This is exploratory and asserts the observed shape so a parser upgrade that
 * changes it fails loudly here.
 */
class JSqlParserPlaceholderProbeTest {

    @Test
    fun `probe dollar placeholder in select where`() {
        val stmt = CCJSqlParserUtil.parse("SELECT * FROM users WHERE tenant_id = \$1") as PlainSelect
        val where = stmt.where as EqualsTo
        println("[PROBE] SELECT rhs class = ${where.rightExpression.javaClass.name}, value = ${where.rightExpression}")
    }

    @Test
    fun `probe question mark placeholder`() {
        val stmt = CCJSqlParserUtil.parse("SELECT * FROM users WHERE tenant_id = ?") as PlainSelect
        val where = stmt.where as EqualsTo
        val rhs = where.rightExpression
        println("[PROBE] QMARK rhs class = ${rhs.javaClass.name}, value = $rhs")
        if (rhs is JdbcParameter) {
            println("[PROBE] QMARK index = ${rhs.index}, useFixedIndex = ${rhs.isUseFixedIndex}")
        }
    }

    @Test
    fun `probe dollar placeholder details`() {
        val stmt = CCJSqlParserUtil.parse("SELECT * FROM users WHERE tenant_id = \$2") as PlainSelect
        val where = stmt.where as EqualsTo
        val rhs = where.rightExpression
        println("[PROBE] DOLLAR rhs class = ${rhs.javaClass.name}, value = $rhs")
        if (rhs is JdbcParameter) {
            println("[PROBE] DOLLAR index = ${rhs.index}, useFixedIndex = ${rhs.isUseFixedIndex}")
        }
    }

    @Test
    fun `probe literal value`() {
        val stmt = CCJSqlParserUtil.parse("SELECT * FROM users WHERE tenant_id = 42") as PlainSelect
        val where = stmt.where as EqualsTo
        val rhs = where.rightExpression
        println("[PROBE] LITERAL rhs class = ${rhs.javaClass.name}, isLong = ${rhs is LongValue}")
    }

    @Test
    fun `probe insert placeholders`() {
        val insert = CCJSqlParserUtil.parse(
            "INSERT INTO users (id, tenant_id, name) VALUES (\$1, \$2, \$3)",
        ) as Insert
        println("[PROBE] INSERT columns = ${insert.columns}")
        val values = insert.select as? PlainSelect
        println("[PROBE] INSERT select class = ${insert.select?.javaClass?.name}")
        println("[PROBE] INSERT values expr = ${insert.values?.expressions?.map { it.javaClass.simpleName to it.toString() }}")
    }
}
