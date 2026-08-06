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
 * Crystal Framework 虚拟 Statement，推迟到 [execute] 时才从对应数据源取真实连接。
 *
 * 工作流程：
 * 1. [bind] / [bindNull] / [add] 时缓存所有调用，不实际执行
 * 2. 捕获分片列绑定的值
 * 3. [execute] 时：
 *    - 提取所有 batch 的分片值，调用 [ShardingAlgorithm] 算出真实表
 *    - 校验所有 batch 必须路由到同一张真实表（单表路由约束）
 *    - 从 [R2dbcConnectionPoolRegistry] 取 [R2dbcShardingRule.dataSourceName] 指定的连接
 *    - 改写 SQL（逻辑表 → 真实表）
 *    - 在真实连接上重放所有 bind/add 调用
 *    - 执行并返回结果
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
     * 分片列在参数列表中的 0-based 索引位置，从 SQL 解析出。
     * 例如 `INSERT INTO users (id, tenant_id, name) VALUES ($1, $2, $3)` 中 tenant_id → index=1
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
        // 1. 收尾当前 batch
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

        // 2. 对每个 batch 算真实表
        val realTables = batches.map { batch ->
            if (!batch.shardingValueSet) {
                throw ShardingException(
                    "Sharded table '${rule.tableName}' batch did not bind sharding column " +
                        "(index=$shardingParamIndex / name=$shardingParamName)",
                )
            }
            resolveRealTable(batch.shardingValue)
        }.toSet()

        // 3. 校验单表约束
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

        // 4. 从目标数据源取真实连接
        val pool = poolRegistry.require(targetDataSource)
        return Flux.from(pool.create()).flatMap { realConnection ->
            // 5. 改写 SQL
            tableNode.name = realTable
            val finalSql = parsed.toString()

            // 6. 建真实 statement 并重放所有调用
            val realStmt = realConnection.createStatement(finalSql)

            statementActions.forEach { it(realStmt) }

            batches.forEachIndexed { index, batch ->
                batch.actions.forEach { it(realStmt) }
                if (index < batches.size - 1) {
                    realStmt.add()
                }
            }

            // 7. 执行
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
     * 从 SQL AST 中定位分片列对应的参数索引（0-based）。
     * 例如 `INSERT INTO users (id, tenant_id, name) VALUES ($1, $2, $3)` 且 shardingColumn="tenant_id"
     * → 在 columns 列表找到 tenant_id 在位置 1 → 对应 $2 → 绑定索引 = 1
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
                // SELECT/UPDATE/DELETE 的分片列在 WHERE 里，位置不固定，需要完整解析 WHERE AST
                // 简化：假设业务 SQL 第一个参数就是分片列（实际项目需完整 WHERE 遍历）
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

    /** 一个 batch 的所有 bind 调用 + 对应的分片值 */
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
