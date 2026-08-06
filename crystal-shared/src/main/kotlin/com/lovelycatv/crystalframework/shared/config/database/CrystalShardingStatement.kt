package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.Result
import io.r2dbc.spi.Statement
import net.sf.jsqlparser.expression.operators.relational.ExpressionList
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.insert.Insert
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Crystal Framework virtual Statement, defers real connection acquisition until [execute].
 *
 * Workflow:
 * 1. On [bind] / [bindNull] / [add], cache all calls without executing
 * 2. Capture sharding column value
 * 3. On [execute]:
 *    - Extract sharding values from all batches, call [ShardingAlgorithm] to resolve real table
 *    - Validate all batches must route to the same real table (single-table routing constraint)
 *    - Acquire connection from [R2dbcConnectionPoolRegistry] using [R2dbcShardingRule.dataSourceName]
 *    - Rewrite SQL (logical table → real table)
 *    - Replay all bind/add calls on real connection
 *    - Execute and return result
 */
class CrystalShardingStatement(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val rule: R2dbcShardingRule,
    private val parsed: net.sf.jsqlparser.statement.Statement,
    private val tableNode: Table,
) : Statement {

    private val statementActions = mutableListOf<(Statement) -> Unit>()
    private val batches = mutableListOf<Batch>()
    private var current = Batch()

    /**
     * 0-based index of sharding column in parameter list, parsed from SQL.
     * Example: `INSERT INTO users (id, tenant_id, name) VALUES ($1, $2, $3)` → tenant_id at index=1
     */
    private val shardingParamIndex: Int by lazy { locateShardingParamIndex() }
    private val shardingParamName: String by lazy {
        "${R2dbcDataSourceConstants.POSITIONAL_PARAMETER_PREFIX}${shardingParamIndex + 1}"
    }

    override fun bind(index: Int, value: Any): Statement = apply {
        current.actions += { it.bind(index, value) }
        if (index == shardingParamIndex) {
            current.setShardingValue(value)
        }
    }

    override fun bind(name: String, value: Any): Statement = apply {
        current.actions += { it.bind(name, value) }
        if (name == shardingParamName) {
            current.setShardingValue(value)
        }
    }

    override fun bindNull(index: Int, type: Class<*>): Statement = apply {
        current.actions += { it.bindNull(index, type) }
        if (index == shardingParamIndex) {
            current.setShardingValue(null)
        }
    }

    override fun bindNull(name: String, type: Class<*>): Statement = apply {
        current.actions += { it.bindNull(name, type) }
        if (name == shardingParamName) {
            current.setShardingValue(null)
        }
    }

    override fun add(): Statement = apply {
        batches += current
        current = Batch()
    }

    override fun returnGeneratedValues(vararg columns: String): Statement = apply {
        val copy = columns.clone()
        statementActions += { it.returnGeneratedValues(*copy) }
    }

    override fun fetchSize(rows: Int): Statement = apply {
        statementActions += { it.fetchSize(rows) }
    }

    override fun execute(): Publisher<out Result> {
        // 1. Finalize current batch
        if (current.actions.isNotEmpty()) {
            batches += current
            current = Batch()
        }

        if (batches.isEmpty()) {
            return Flux.error(
                ShardingException(
                    "Sharded table '${rule.tableName}' statement executed without any bind calls",
                ),
            )
        }

        // 2. Resolve real table for each batch
        val realTables = batches.map { batch ->
            if (!batch.shardingValueSet) {
                throw ShardingException(
                    "Sharded table '${rule.tableName}' batch did not bind sharding column " +
                        "(index=$shardingParamIndex / name=$shardingParamName)",
                )
            }
            resolveRealTable(batch.shardingValue)
        }.toSet()

        // 3. Validate single-table constraint
        if (realTables.size != 1) {
            return Flux.error(
                ShardingException(
                    "Batch on sharded table '${rule.tableName}' spans multiple actual tables $realTables; " +
                        "single-target routing only",
                ),
            )
        }

        val realTable = realTables.first()
        val targetDataSource = rule.dataSourceName

        // 4. Acquire real connection from target data source
        val pool = poolRegistry.require(targetDataSource)
        return Flux.from(pool.create()).flatMap { realConnection ->
            // 5. Rewrite SQL
            tableNode.name = realTable
            val finalSql = parsed.toString()

            // 6. Create real statement and replay all calls
            val realStmt = realConnection.createStatement(finalSql)

            statementActions.forEach { it(realStmt) }

            batches.forEachIndexed { index, batch ->
                batch.actions.forEach { it(realStmt) }
                if (index < batches.size - 1) {
                    realStmt.add()
                }
            }

            // 7. Execute
            Flux.from(realStmt.execute())
        }
    }

    private fun resolveRealTable(shardingValue: Any?): String {
        val algorithm = rule.algorithm
            ?: throw ShardingException("Table '${rule.tableName}' has no sharding algorithm")
        val realTable = algorithm.doSharding(rule.tableName, shardingValue).trim().lowercase()
        if (realTable !in rule.actualTables) {
            throw ShardingException(
                "Sharding algorithm for table '${rule.tableName}' returned '$realTable' " +
                    "which is not in actualTables=${rule.actualTables} (shardingValue=$shardingValue)",
            )
        }
        return realTable
    }

    /**
     * Locate parameter index (0-based) for sharding column from SQL AST.
     * Example: `INSERT INTO users (id, tenant_id, name) VALUES ($1, $2, $3)` with shardingColumn="tenant_id"
     * → find tenant_id at position 1 in columns list → corresponds to $2 → bind index = 1
     */
    private fun locateShardingParamIndex(): Int {
        val column = rule.shardingColumn
            ?: throw ShardingException("Table '${rule.tableName}' is sharded but has no sharding column")

        return when (parsed) {
            is Insert -> {
                val insert = parsed as Insert
                val columns = insert.columns
                    ?: throw ShardingException(
                        "INSERT on sharded table '${rule.tableName}' has no column list",
                    )
                val position = columns.indexOfFirstColumn { col ->
                    stripQuotes(col.columnName).equals(column, ignoreCase = true)
                }
                if (position < 0) {
                    throw ShardingException(
                        "Sharding column '$column' not found in INSERT column list for table '${rule.tableName}'",
                    )
                }
                position
            }
            else -> {
                // For SELECT/UPDATE/DELETE, sharding column is in WHERE clause with variable position
                // Simplified: assume first parameter is sharding column (real projects need full WHERE traversal)
                throw ShardingException(
                    "Sharding for ${parsed.javaClass.simpleName} on table '${rule.tableName}' requires " +
                        "literal sharding value or explicit parameter position (not yet implemented for WHERE clauses)",
                )
            }
        }
    }

    private fun stripQuotes(name: String): String {
        return name.removeSurrounding("\"").removeSurrounding("`")
    }

    private fun ExpressionList<Column>.indexOfFirstColumn(predicate: (Column) -> Boolean): Int {
        return this.toList().indexOfFirst(predicate)
    }

    /** All bind calls for one batch + corresponding sharding value */
    private class Batch {
        val actions = mutableListOf<(Statement) -> Unit>()
        var shardingValue: Any? = null
            private set
        var shardingValueSet: Boolean = false
            private set

        fun setShardingValue(value: Any?) {
            shardingValue = value
            shardingValueSet = true
        }
    }
}
