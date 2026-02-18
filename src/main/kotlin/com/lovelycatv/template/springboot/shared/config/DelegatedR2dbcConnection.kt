package com.lovelycatv.template.springboot.shared.config

import com.lovelycatv.vertex.log.logger
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Statement
import net.sf.jsqlparser.expression.LongValue
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.update.Update

class DelegatedR2dbcConnection(private val delegate: Connection) : Connection by delegate {
    private val logger = logger()

    override fun createStatement(p0: String): Statement {
        val statement = try {
            CCJSqlParserUtil.parse(p0)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while parsing SQL from $p0", e)
        }

        var modified = true
        val sql = when (statement) {
            is PlainSelect -> {
                addSoftDeleteConditionWithParser(statement)
            }

            is Update -> {
                modifyUpdateSql(statement)
            }

            is Delete -> {
                convertDeleteToSoftDelete(statement)
            }

            else -> {
                modified = false
                p0
            }
        }

        if (modified) {
            logger.info("Modified SQL:")
            logger.info("        from: $p0")
            logger.info("          to: $sql")
        } else {
            logger.warn("Unsupported statement type: ${statement.javaClass.name}")
        }

        return delegate.createStatement(sql)
    }

    fun addSoftDeleteConditionWithParser(statement: PlainSelect): String {
        val deletedTimeColumn = Column("deleted_time")
        val isNullExpr = IsNullExpression(deletedTimeColumn)

        when (val where = statement.where) {
            null -> {
                statement.where = isNullExpr
            }
            else -> {
                statement.where = AndExpression(
                    ParenthesedExpressionList(where),
                    isNullExpr
                )
            }
        }

        return statement.toString()
    }

    fun modifyUpdateSql(statement: Update): String {
        val now = System.currentTimeMillis()

        statement.addUpdateSet(Column("modified_time"), LongValue(now))

        return statement.toString()
    }

    fun convertDeleteToSoftDelete(statement: Delete): String {
        val now = System.currentTimeMillis()

        val update = Update()

        update.table = statement.table

        update.where = statement.where

        update.addUpdateSet(
            Column("modified_time"),
            LongValue(now)
        )

        update.addUpdateSet(
            Column("deleted_time"),
            LongValue(now)
        )

        return update.toString()
    }
}