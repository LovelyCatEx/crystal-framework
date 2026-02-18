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
        val sql = if (p0.startsWith("SELECT")) {
            addSoftDeleteConditionWithParser(p0)
        } else if (p0.startsWith("UPDATE")) {
            modifyUpdateSql(p0)
        } else if (p0.startsWith("DELETE")) {
            convertDeleteToSoftDelete(p0)
        } else {
            p0
        }

        logger.info("Modified SQL:")
        logger.info("        from: $p0")
        logger.info("          to: $sql")

        return delegate.createStatement(sql)
    }

    fun addSoftDeleteConditionWithParser(sql: String): String {
        return try {
            val statement = CCJSqlParserUtil.parse(sql)

            if (statement is PlainSelect) {
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

                statement.toString()
            } else {
                sql
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while parsing SQL from $sql", e)
        }
    }

    fun modifyUpdateSql(sql: String): String {
        return try {
            val statement = CCJSqlParserUtil.parse(sql)

            if (statement is Update) {
                val now = System.currentTimeMillis()

                statement.addUpdateSet(Column("modified_time"), LongValue(now))

                statement.toString()
            } else {
                sql
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while parsing SQL from $sql", e)
        }
    }

    fun convertDeleteToSoftDelete(sql: String): String {
        return try {
            val statement = CCJSqlParserUtil.parse(sql)

            if (statement is Delete) {
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

                update.toString()
            } else {
                sql
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while parsing SQL from $sql", e)
        }
    }
}