package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.vertex.log.logger
import net.sf.jsqlparser.expression.LongValue
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.update.Update
import java.util.concurrent.ConcurrentHashMap

object CrystalFrameworkSQLModifier {
    private val logger = logger()
    private val baseEntityTables = ConcurrentHashMap<String, Boolean>()

    fun registerBaseEntityTables(tables: Collection<String>) {
        tables.forEach { baseEntityTables[it.lowercase()] = true }
    }

    fun processSql(p0: String): String {
        val statement = try {
            CCJSqlParserUtil.parse(p0)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error while parsing SQL from $p0", e)
        }

        val tableName = extractTargetTableName(statement)
        if (tableName != null && !baseEntityTables.containsKey(tableName)) {
            logger.debug("Skipping non-BaseEntity table [{}]", tableName)
            return p0
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
            logger.debug("Modified SQL:")
            logger.debug("        from: $p0")
            logger.debug("          to: $sql")
        } else {
            logger.debug("Unsupported statement type: ${statement.javaClass.name}, sql: $sql")
        }

        return sql
    }

    private fun extractTargetTableName(statement: Any): String? {
        return when (statement) {
            is PlainSelect -> {
                val fromItem = statement.fromItem
                if (fromItem is Table) fromItem.name.lowercase() else null
            }
            is Update -> statement.table.name.lowercase()
            is Delete -> statement.table.name.lowercase()
            else -> null
        }
    }

    fun addSoftDeleteConditionWithParser(statement: PlainSelect): String {
       if (statement.fromItem?.toString() == null) {
           return statement.toString()
       }

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

        if (!statement.updateSets.any {
                it.columns.any { it.columnName == "modified_time" || it.columnName == "\"modified_time\"" }
            }) {
            statement.addUpdateSet(Column("modified_time"), LongValue(now))
        }

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