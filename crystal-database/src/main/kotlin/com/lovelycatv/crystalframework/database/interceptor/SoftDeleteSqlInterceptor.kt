package com.lovelycatv.crystalframework.database.interceptor

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.vertex.log.logger
import net.sf.jsqlparser.expression.LongValue
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.update.Update
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Built-in interceptor implementing the framework's soft-delete semantics for [BaseEntity] tables:
 *
 * - `SELECT` — appends `deleted_time IS NULL` so soft-deleted rows stay hidden.
 * - `UPDATE` — maintains `modified_time` when the statement does not already set it.
 * - `DELETE` — rewritten into an `UPDATE` that stamps `deleted_time` and `modified_time`.
 *
 * The BaseEntity-table gating lives here (not in [com.lovelycatv.crystalframework.database.utils.CrystalFrameworkSQLModifier]): only tables
 * registered via [registerBaseEntityTables] are touched, every other table is left unchanged so
 * other interceptors remain free to act on non-BaseEntity tables.
 */
@Component
class SoftDeleteSqlInterceptor : SqlStatementInterceptor {
    private val logger = logger()
    private val baseEntityTables = ConcurrentHashMap<String, Boolean>()

    fun registerBaseEntityTables(tables: Collection<String>) {
        tables.forEach { baseEntityTables[it.lowercase()] = true }
    }

    override fun intercept(statement: Statement, tableName: String?): Statement {
        if (tableName != null && !baseEntityTables.containsKey(tableName)) {
            logger.debug("Skipping non-BaseEntity table [{}]", tableName)
            return statement
        }

        return when (statement) {
            is PlainSelect -> addSoftDeleteCondition(statement)
            is Update -> maintainModifiedTime(statement)
            is Delete -> convertDeleteToSoftDelete(statement)
            else -> statement
        }
    }

    private fun addSoftDeleteCondition(statement: PlainSelect): Statement {
        if (statement.fromItem?.toString() == null) {
            return statement
        }

        val isNullExpr = IsNullExpression(Column(BaseEntity.DELETED_TIME))

        statement.where = when (val where = statement.where) {
            null -> isNullExpr
            else -> AndExpression(ParenthesedExpressionList(where), isNullExpr)
        }

        return statement
    }

    private fun maintainModifiedTime(statement: Update): Statement {
        val alreadySet = statement.updateSets.any { set ->
            set.columns.any {
                it.columnName == BaseEntity.MODIFIED_TIME || it.columnName == "\"${BaseEntity.MODIFIED_TIME}\""
            }
        }
        if (!alreadySet) {
            statement.addUpdateSet(Column(BaseEntity.MODIFIED_TIME), LongValue(System.currentTimeMillis()))
        }
        return statement
    }

    private fun convertDeleteToSoftDelete(statement: Delete): Statement {
        val now = System.currentTimeMillis()

        return Update().apply {
            table = statement.table
            where = statement.where
            addUpdateSet(Column(BaseEntity.MODIFIED_TIME), LongValue(now))
            addUpdateSet(Column(BaseEntity.DELETED_TIME), LongValue(now))
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
