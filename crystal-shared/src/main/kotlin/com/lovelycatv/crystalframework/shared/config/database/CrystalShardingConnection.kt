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
 * Crystal Framework virtual connection, does not bind to any real physical connection.
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
) : Connection {

    /**
     * Real connection from primary data source, used for transaction management, metadata queries, etc.
     * Lazy-loaded, acquired only on first call to transaction methods.
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
