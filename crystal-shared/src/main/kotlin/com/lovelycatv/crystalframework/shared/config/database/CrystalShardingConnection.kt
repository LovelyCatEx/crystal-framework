package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.Batch
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionMetadata
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.Statement
import io.r2dbc.spi.TransactionDefinition
import io.r2dbc.spi.ValidationDepth
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.update.Update
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Crystal Framework 虚拟连接，不绑定任何真实物理连接。
 *
 * 在 [createStatement] 时解析 SQL、查询分片规则：
 * - 有表分片规则 → 返回 [CrystalShardingStatement]（虚拟 Statement）
 * - 无表分片 / 解析失败 → 从对应数据源取真实连接并直接建 statement
 *
 * 事务方法（begin/commit/rollback）委托给 primary 数据源的连接。跨数据源事务不在本版本支持范围。
 */
class CrystalShardingConnection(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val shardingRuleRegistry: R2dbcShardingRuleRegistry,
) : Connection {

    /**
     * Primary 数据源的真实连接，用于事务管理、元数据查询等。懒加载，首次调用事务方法时才取。
     */
    @Volatile
    private var primaryDelegate: Connection? = null

    private fun getPrimaryDelegate(): Connection {
        return primaryDelegate ?: synchronized(this) {
            primaryDelegate ?: run {
                val conn = Mono.from(poolRegistry.require(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME).create())
                    .block() ?: throw IllegalStateException("Failed to acquire primary connection")
                primaryDelegate = conn
                conn
            }
        }
    }

    override fun createStatement(sql: String): Statement {
        // 1. 文本层改写（软删除 deletedTime IS NULL、modifiedTime 等）
        val processedSql = CrystalFrameworkSQLModifier.processSql(sql)

        // 2. 解析 SQL AST
        val parsed = runCatching { CCJSqlParserUtil.parse(processedSql) }.getOrNull()
            ?: return delegateToDataSource(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, processedSql)

        // 3. 提取表节点
        val tableNode = extractTableNode(parsed)
            ?: return delegateToDataSource(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, processedSql)

        val logicalTable = stripQuotes(tableNode.name).lowercase()

        // 4. 查分片规则
        val rule = shardingRuleRegistry.find(logicalTable)?.rule

        // 5. 路由决策
        return if (rule?.tableShardingEnabled == true) {
            // 有表分片 → 返回虚拟 Statement，推迟到 execute() 时取真实连接
            CrystalShardingStatement(
                poolRegistry = poolRegistry,
                rule = rule,
                parsed = parsed,
                tableNode = tableNode,
            )
        } else {
            // 无表分片 → 直接从对应数据源取连接建 statement
            val targetDataSource = rule?.dataSourceName ?: R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME
            delegateToDataSource(targetDataSource, processedSql)
        }
    }

    private fun delegateToDataSource(dataSourceName: String, sql: String): Statement {
        val pool = poolRegistry.require(dataSourceName)
        val connection = Mono.from(pool.create()).block()
            ?: throw IllegalStateException("Failed to acquire connection from $dataSourceName")
        return connection.createStatement(sql)
    }

    private fun extractTableNode(statement: net.sf.jsqlparser.statement.Statement): Table? {
        return when (statement) {
            is PlainSelect -> statement.fromItem as? Table
            is Update -> statement.table
            is Delete -> statement.table
            is Insert -> statement.table
            else -> null
        }
    }

    private fun stripQuotes(name: String): String {
        return name.removeSurrounding("\"").removeSurrounding("`")
    }

    // 事务方法委托给 primary
    override fun beginTransaction(): Publisher<Void> = getPrimaryDelegate().beginTransaction()
    override fun beginTransaction(definition: TransactionDefinition): Publisher<Void> =
        getPrimaryDelegate().beginTransaction(definition)
    override fun commitTransaction(): Publisher<Void> = getPrimaryDelegate().commitTransaction()
    override fun rollbackTransaction(): Publisher<Void> = getPrimaryDelegate().rollbackTransaction()
    override fun setTransactionIsolationLevel(isolationLevel: IsolationLevel): Publisher<Void> =
        getPrimaryDelegate().setTransactionIsolationLevel(isolationLevel)
    override fun createSavepoint(name: String): Publisher<Void> = getPrimaryDelegate().createSavepoint(name)
    override fun releaseSavepoint(name: String): Publisher<Void> = getPrimaryDelegate().releaseSavepoint(name)
    override fun rollbackTransactionToSavepoint(name: String): Publisher<Void> =
        getPrimaryDelegate().rollbackTransactionToSavepoint(name)

    override fun createBatch(): Batch = getPrimaryDelegate().createBatch()
    override fun setAutoCommit(autoCommit: Boolean): Publisher<Void> = getPrimaryDelegate().setAutoCommit(autoCommit)
    override fun setLockWaitTimeout(timeout: Duration): Publisher<Void> = getPrimaryDelegate().setLockWaitTimeout(timeout)
    override fun setStatementTimeout(timeout: Duration): Publisher<Void> = getPrimaryDelegate().setStatementTimeout(timeout)

    override fun close(): Publisher<Void> {
        // Only close if primary connection was actually acquired
        return primaryDelegate?.close() ?: Mono.empty()
    }

    override fun validate(depth: ValidationDepth): Publisher<Boolean> = getPrimaryDelegate().validate(depth)
    override fun getMetadata(): ConnectionMetadata = getPrimaryDelegate().metadata
    override fun isAutoCommit(): Boolean = getPrimaryDelegate().isAutoCommit
    override fun getTransactionIsolationLevel(): IsolationLevel = getPrimaryDelegate().transactionIsolationLevel
}
