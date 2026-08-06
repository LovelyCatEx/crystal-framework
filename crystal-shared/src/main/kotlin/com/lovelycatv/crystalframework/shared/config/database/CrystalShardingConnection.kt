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
 * Crystal Framework virtual connection wrapper.
 *
 * On [createStatement], parses SQL and queries sharding rules:
 * - Has table sharding rule → returns [CrystalShardingStatement] (virtual Statement)
 * - No table sharding / parse failed → returns [DelegatedStatement] (deferred connection)
 *
 * Transaction methods (begin/commit/rollback) delegate to primary data source connection.
 * Cross-data-source transactions are not supported in this version.
 */
class CrystalShardingConnection(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val shardingRuleRegistry: R2dbcShardingRuleRegistry,
    private val primaryDelegate: Connection,
) : Connection {

    override fun createStatement(sql: String): Statement {
        // 1. Text-level rewriting (soft delete: deletedTime IS NULL, modifiedTime, etc.)
        val processedSql = CrystalFrameworkSQLModifier.processSql(sql)

        // 2. Parse SQL AST
        val parsed = runCatching { CCJSqlParserUtil.parse(processedSql) }.getOrNull()
            ?: return delegateToDataSource(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, processedSql)

        // 3. Extract table node
        val tableNode = extractTableNode(parsed)
            ?: return delegateToDataSource(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, processedSql)

        val logicalTable = stripQuotes(tableNode.name).lowercase()

        // 4. Query sharding rule
        val rule = shardingRuleRegistry.find(logicalTable)?.rule

        // 5. Routing decision
        return if (rule?.tableShardingEnabled == true) {
            // Has table sharding → return virtual Statement, defer real connection to execute()
            CrystalShardingStatement(
                poolRegistry = poolRegistry,
                rule = rule,
                parsed = parsed,
                tableNode = tableNode,
            )
        } else {
            // No table sharding → delegate to data source with deferred connection
            val targetDataSource = rule?.dataSourceName ?: R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME
            delegateToDataSource(targetDataSource, processedSql)
        }
    }

    private fun delegateToDataSource(dataSourceName: String, sql: String): Statement {
        val pool = poolRegistry.require(dataSourceName)
        return DelegatedStatement(pool, sql)
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

    // Transaction methods delegate to primary
    override fun beginTransaction(): Publisher<Void> = primaryDelegate.beginTransaction()
    override fun beginTransaction(definition: TransactionDefinition): Publisher<Void> =
        primaryDelegate.beginTransaction(definition)
    override fun commitTransaction(): Publisher<Void> = primaryDelegate.commitTransaction()
    override fun rollbackTransaction(): Publisher<Void> = primaryDelegate.rollbackTransaction()
    override fun setTransactionIsolationLevel(isolationLevel: IsolationLevel): Publisher<Void> =
        primaryDelegate.setTransactionIsolationLevel(isolationLevel)
    override fun createSavepoint(name: String): Publisher<Void> = primaryDelegate.createSavepoint(name)
    override fun releaseSavepoint(name: String): Publisher<Void> = primaryDelegate.releaseSavepoint(name)
    override fun rollbackTransactionToSavepoint(name: String): Publisher<Void> =
        primaryDelegate.rollbackTransactionToSavepoint(name)

    override fun createBatch(): Batch = primaryDelegate.createBatch()
    override fun setAutoCommit(autoCommit: Boolean): Publisher<Void> = primaryDelegate.setAutoCommit(autoCommit)
    override fun setLockWaitTimeout(timeout: Duration): Publisher<Void> = primaryDelegate.setLockWaitTimeout(timeout)
    override fun setStatementTimeout(timeout: Duration): Publisher<Void> = primaryDelegate.setStatementTimeout(timeout)

    override fun close(): Publisher<Void> = primaryDelegate.close()

    override fun validate(depth: ValidationDepth): Publisher<Boolean> = primaryDelegate.validate(depth)
    override fun getMetadata(): ConnectionMetadata = primaryDelegate.metadata
    override fun isAutoCommit(): Boolean = primaryDelegate.isAutoCommit
    override fun getTransactionIsolationLevel(): IsolationLevel = primaryDelegate.transactionIsolationLevel
}
