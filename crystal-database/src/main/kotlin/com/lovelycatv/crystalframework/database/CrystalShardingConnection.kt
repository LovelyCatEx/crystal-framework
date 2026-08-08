package com.lovelycatv.crystalframework.database

import co.elastic.apm.api.Span
import com.lovelycatv.crystalframework.database.utils.CrystalFrameworkSQLModifier
import com.lovelycatv.crystalframework.database.constants.R2dbcDataSourceConstants
import com.lovelycatv.crystalframework.database.transaction.TransactionConnectionHolder
import com.lovelycatv.crystalframework.database.sharding.R2dbcShardingRuleRegistry
import com.lovelycatv.crystalframework.shared.config.observability.DistributedTransactionLabel
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
 * Crystal Framework virtual connection wrapper with distributed transaction support.
 *
 * On [createStatement], parses SQL and queries sharding rules:
 * - Has table sharding rule → returns [CrystalShardingStatement] (virtual Statement)
 * - No table sharding / parse failed → returns [DelegatedStatement] (deferred connection)
 *
 * Transaction methods manage multiple data source connections:
 * - beginTransaction() creates TransactionConnectionHolder
 * - Statements acquire connections lazily from holder
 * - commitTransaction() commits all connections (rollback all if any fails)
 * - rollbackTransaction() rollbacks all connections
 *
 * Note: This is NOT true XA. If one connection commits successfully but another fails,
 * data inconsistency can occur. For production use, integrate Seata or XA coordinator.
 */
class CrystalShardingConnection(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val shardingRuleRegistry: R2dbcShardingRuleRegistry,
    private val traceHeaders: Map<String, String>,
    private val transactionLabel: DistributedTransactionLabel,
    private val apmParentSpan: Span?,
) : Connection {

    @Volatile
    private var transactionHolder: TransactionConnectionHolder? = null

    override fun beginTransaction(): Publisher<Void> {
        return Mono.fromRunnable {
            val holder = TransactionConnectionHolder(poolRegistry, traceHeaders, transactionLabel, apmParentSpan)
            holder.markTransactionStart()
            transactionHolder = holder
        }
    }

    override fun commitTransaction(): Publisher<Void> {
        return Mono.defer {
            val holder = transactionHolder
            // Two-Phase Commit: PREPARE then COMMIT PREPARED. Real connections are returned to
            // the pool by close() -> holder.closeAll(); do NOT drop the holder here, or close()
            // can no longer reach it and the underlying pooled connections leak.
            holder?.prepareAll()
                ?.then(holder.commitPrepared())
                ?: Mono.empty()
        }
    }

    override fun rollbackTransaction(): Publisher<Void> {
        return Mono.defer {
            val holder = transactionHolder
            // Rollback prepared then non-prepared transactions. The holder is released by
            // close(), not here (see commitTransaction).
            holder?.rollbackPrepared()
                ?.then(holder.rollbackAll())
                ?: Mono.empty()
        }
    }

    override fun close(): Publisher<Void> {
        return Mono.defer {
            val holder = transactionHolder
            holder?.closeAll()?.doFinally { transactionHolder = null } ?: Mono.empty()
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
                transactionHolder = transactionHolder,
            )
        } else {
            // No table sharding → delegate to data source with deferred connection
            val targetDataSource = rule?.dataSourceName ?: R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME
            delegateToDataSource(targetDataSource, processedSql)
        }
    }

    private fun delegateToDataSource(dataSourceName: String, sql: String): Statement {
        val holder = transactionHolder
        return if (holder != null) {
            // In transaction: use holder to get connection
            DelegatedStatement(poolRegistry.require(dataSourceName), sql, holder, dataSourceName)
        } else {
            // No transaction: temporary connection
            DelegatedStatement(poolRegistry.require(dataSourceName), sql)
        }
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

    // Other Connection methods delegate to primary pool (for metadata operations)
    override fun beginTransaction(definition: TransactionDefinition): Publisher<Void> {
        return beginTransaction()  // Ignore definition for now
    }

    override fun setTransactionIsolationLevel(isolationLevel: IsolationLevel): Publisher<Void> {
        return Mono.empty()  // Isolation level should be set per connection in holder
    }

    override fun createSavepoint(name: String): Publisher<Void> {
        return Mono.error(UnsupportedOperationException("Savepoints not supported in distributed transactions"))
    }

    override fun releaseSavepoint(name: String): Publisher<Void> {
        return Mono.error(UnsupportedOperationException("Savepoints not supported in distributed transactions"))
    }

    override fun rollbackTransactionToSavepoint(name: String): Publisher<Void> {
        return Mono.error(UnsupportedOperationException("Savepoints not supported in distributed transactions"))
    }

    override fun createBatch(): Batch {
        throw UnsupportedOperationException("Batch operations should use createStatement()")
    }

    override fun setAutoCommit(autoCommit: Boolean): Publisher<Void> {
        return Mono.empty()  // Managed by transaction holder
    }

    override fun setLockWaitTimeout(timeout: Duration): Publisher<Void> {
        return Mono.empty()
    }

    override fun setStatementTimeout(timeout: Duration): Publisher<Void> {
        return Mono.empty()
    }

    override fun validate(depth: ValidationDepth): Publisher<Boolean> {
        return Mono.just(true)
    }

    override fun getMetadata(): ConnectionMetadata {
        // Create a simple ConnectionMetadata implementation
        return object : ConnectionMetadata {
            override fun getDatabaseProductName(): String = "Crystal Sharding Virtual Connection"
            override fun getDatabaseVersion(): String = "1.0"
        }
    }

    override fun isAutoCommit(): Boolean = false

    override fun getTransactionIsolationLevel(): IsolationLevel = IsolationLevel.READ_COMMITTED
}
