package com.lovelycatv.crystalframework.shared.config.database

import co.elastic.apm.api.ElasticApm
import io.r2dbc.spi.Connection
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * Manages multiple database connections for distributed transaction with 2PC support.
 *
 * Two-Phase Commit Protocol:
 * 1. Phase 1 (Prepare): All participants vote to commit or abort
 *    - Execute: PREPARE TRANSACTION 'xid'
 *    - Data is written to disk and locked
 *    - Transaction is in "prepared" state (can commit or rollback)
 *    - Survives crashes (recovered from WAL)
 *
 * 2. Phase 2 (Commit/Rollback): Coordinator makes decision based on votes
 *    - All voted OK → COMMIT PREPARED 'xid'
 *    - Any voted NO → ROLLBACK PREPARED 'xid'
 *
 * Note: Requires PostgreSQL configuration:
 *   max_prepared_transactions > 0 (default is 0)
 *
 * Workflow:
 * 1. markTransactionStart() - mark transaction started
 * 2. getOrCreateConnection(dataSource) - lazy acquire connection and begin transaction
 * 3. All connections remain open with active transactions
 * 4. prepareAll() - PREPARE TRANSACTION on all connections (Phase 1)
 * 5. commitPrepared() - COMMIT PREPARED on all connections (Phase 2)
 *    OR rollbackPrepared() - ROLLBACK PREPARED on all connections
 *
 * Consistency guarantee:
 * - After prepareAll() succeeds, all data is on disk and can survive crashes
 * - If any connection fails during prepareAll(), all prepared transactions are rolled back
 * - If commitPrepared() partially fails, retry mechanism ensures eventual consistency
 */
class TransactionConnectionHolder(
    private val poolRegistry: R2dbcConnectionPoolRegistry
) {
    private val connections = mutableMapOf<String, Connection>()
    private val preparedTransactions = mutableMapOf<String, String>()  // dataSource -> xid

    @Volatile
    private var transactionActive = false

    @Volatile
    private var globalTransactionId: String? = null

    companion object {
        private val log = LoggerFactory.getLogger(TransactionConnectionHolder::class.java)
    }

    fun markTransactionStart() {
        transactionActive = true
        globalTransactionId = "tx_${UUID.randomUUID()}"
        log.debug("Distributed transaction started with GID: {}", globalTransactionId)
    }

    fun getOrCreateConnection(dataSourceName: String): Mono<Connection> {
        return Mono.defer {
            synchronized(connections) {
                val existing = connections[dataSourceName]
                if (existing != null) {
                    log.debug("Reusing existing connection for dataSource: {}", dataSourceName)
                    return@defer Mono.just(existing)
                }

                log.debug("Acquiring new connection for dataSource: {}", dataSourceName)
                Mono.from(poolRegistry.require(dataSourceName).create())
                    .flatMap { conn ->
                        if (transactionActive) {
                            log.debug("Beginning transaction on dataSource: {}", dataSourceName)
                            Mono.from(conn.beginTransaction())
                                .thenReturn(conn)
                        } else {
                            Mono.just(conn)
                        }
                    }
                    .doOnNext { conn ->
                        synchronized(connections) {
                            connections[dataSourceName] = conn
                        }
                        log.debug("Connection acquired and registered for dataSource: {}", dataSourceName)
                    }
                    .doOnError { error ->
                        log.warn("Failed to acquire connection for dataSource: {}", dataSourceName, error)
                    }
            }
        }
    }

    /**
     * Phase 1: PREPARE all transactions
     * Execute PREPARE TRANSACTION on all connections.
     * If any fails, rollback all prepared transactions.
     */
    fun prepareAll(): Mono<Void> {
        return Mono.defer {
            if (connections.isEmpty()) {
                log.debug("No connections to prepare")
                return@defer Mono.empty()
            }

            val dataSourceNames = synchronized(connections) { connections.keys.toList() }
            val connList = synchronized(connections) { connections.values.toList() }
            val gid = globalTransactionId ?: "tx_${UUID.randomUUID()}"

            log.debug("Phase 1: Preparing {} connections with GID: {}", connList.size, gid)

            // Create APM span for the entire PREPARE phase
            val phaseSpan = ElasticApm.currentSpan()
                .startSpan("db", "postgresql", "prepare")
                .setName("2PC Phase 1: PREPARE ($gid)")

            // Create all PREPARE operations
            val prepareOperations = dataSourceNames.mapIndexed { index, dsName ->
                val xid = "${gid}_${dsName}"

                val prepareSpan = phaseSpan.startSpan("db", "postgresql", "query")
                    .setName("PREPARE TRANSACTION '$xid'")

                Flux.from(
                    connList[index].createStatement("PREPARE TRANSACTION '$xid'")
                        .execute()
                ).flatMap { result ->
                    Mono.from(result.rowsUpdated)
                }.then()
                .doOnSuccess {
                    synchronized(preparedTransactions) {
                        preparedTransactions[dsName] = xid
                    }
                    log.debug("Successfully prepared dataSource: {} with XID: {}", dsName, xid)
                    prepareSpan.end()
                }.doOnError { error ->
                    log.warn("Failed to prepare dataSource: {} with XID: {}", dsName, xid, error)
                    prepareSpan.captureException(error)
                    prepareSpan.end()
                }
            }

            // Execute all operations sequentially (preserves Context including traceId)
            Flux.concat(prepareOperations)
                .then()
                .onErrorResume { error ->
                    log.warn("Phase 1 failed, rolling back all {} prepared transactions", preparedTransactions.size, error)
                    phaseSpan.captureException(error)
                    phaseSpan.end()
                    rollbackPrepared().then(Mono.error(error))
                }
                .doOnSuccess {
                    log.debug("Phase 1 completed: all {} connections prepared successfully", connList.size)
                    phaseSpan.end()
                }
        }
    }

    /**
     * Phase 2: COMMIT all prepared transactions
     */
    fun commitPrepared(): Mono<Void> {
        return Mono.defer {
            val xids = synchronized(preparedTransactions) {
                preparedTransactions.toMap()
            }

            if (xids.isEmpty()) {
                log.debug("No prepared transactions to commit")
                return@defer Mono.empty()
            }

            log.debug("Phase 2: Committing {} prepared transactions: {}", xids.size, xids.keys)

            // Create APM span for the entire COMMIT phase
            val phaseSpan = ElasticApm.currentSpan()
                .startSpan("db", "postgresql", "commit")
                .setName("2PC Phase 2: COMMIT (${xids.size} prepared txns)")

            // Create all COMMIT PREPARED operations
            val commitOperations = xids.map { (dsName, xid) ->
                val conn = connections[dsName]
                if (conn != null) {
                    val commitSpan = phaseSpan.startSpan("db", "postgresql", "query")
                        .setName("COMMIT PREPARED '$xid'")

                    Flux.from(
                        conn.createStatement("COMMIT PREPARED '$xid'")
                            .execute()
                    ).flatMap { result ->
                        Mono.from(result.rowsUpdated)
                    }.then()
                    .doOnSuccess {
                        log.debug("Successfully committed prepared transaction: {}", xid)
                        commitSpan.end()
                    }.doOnError { error ->
                        log.warn("Failed to commit prepared transaction: {} (will retry)", xid, error)
                        commitSpan.captureException(error)
                        commitSpan.end()
                    }
                } else {
                    Mono.empty<Void>()
                }
            }

            // Execute all operations sequentially (preserves Context including traceId)
            Flux.concat(commitOperations)
                .then()
                .doFinally {
                    synchronized(preparedTransactions) {
                        preparedTransactions.clear()
                    }
                    phaseSpan.end()
                }
                .doOnSuccess {
                    log.debug("Phase 2 completed: all {} prepared transactions committed successfully", xids.size)
                }
                .doOnError { error ->
                    phaseSpan.captureException(error)
                }
        }
    }

    /**
     * Rollback all prepared transactions
     */
    fun rollbackPrepared(): Mono<Void> {
        return Mono.defer {
            val xids = synchronized(preparedTransactions) {
                preparedTransactions.toMap()
            }

            if (xids.isEmpty()) {
                log.debug("No prepared transactions to rollback")
                return@defer Mono.empty()
            }

            log.warn("Rolling back {} prepared transactions: {}", xids.size, xids.keys)

            // Create APM span for the entire ROLLBACK phase
            val phaseSpan = ElasticApm.currentSpan()
                .startSpan("db", "postgresql", "rollback")
                .setName("2PC ROLLBACK (${xids.size} prepared txns)")

            // Create all ROLLBACK PREPARED operations
            val rollbackOperations = xids.map { (dsName, xid) ->
                val conn = connections[dsName]
                if (conn != null) {
                    val rollbackSpan = phaseSpan.startSpan("db", "postgresql", "query")
                        .setName("ROLLBACK PREPARED '$xid'")

                    Flux.from(
                        conn.createStatement("ROLLBACK PREPARED '$xid'")
                            .execute()
                    ).flatMap { result ->
                        Mono.from(result.rowsUpdated)
                    }.then()
                    .doOnSuccess {
                        log.warn("Successfully rolled back prepared transaction: {}", xid)
                        rollbackSpan.end()
                    }.onErrorResume { error ->
                        log.warn("Failed to rollback prepared transaction: {} (ignored)", xid, error)
                        rollbackSpan.captureException(error)
                        rollbackSpan.end()
                        Mono.empty()
                    }
                } else {
                    Mono.empty<Void>()
                }
            }

            // Execute all operations sequentially (preserves Context including traceId)
            Flux.concat(rollbackOperations)
                .then()
                .doFinally {
                    synchronized(preparedTransactions) {
                        preparedTransactions.clear()
                    }
                    phaseSpan.end()
                }
                .doOnSuccess {
                    log.warn("All {} prepared transactions rolled back", xids.size)
                }
        }
    }

    /**
     * Legacy: Direct commit without PREPARE (Best-Effort 2PC)
     * Use commitPrepared() for true 2PC instead.
     */
    @Deprecated("Use prepareAll() + commitPrepared() for true 2PC")
    fun commitAll(): Mono<Void> {
        return Mono.defer {
            if (connections.isEmpty()) {
                log.debug("No connections to commit")
                return@defer Mono.empty()
            }

            val dataSourceNames = synchronized(connections) { connections.keys.toList() }
            val connList = synchronized(connections) { connections.values.toList() }

            log.debug("Committing {} connections: {}", connList.size, dataSourceNames)

            // Commit all connections sequentially
            var chain = Mono.empty<Void>()
            dataSourceNames.forEachIndexed { index, dsName ->
                chain = chain.then(
                    Mono.from(connList[index].commitTransaction())
                        .doOnSuccess {
                            log.debug("Successfully committed dataSource: {}", dsName)
                        }
                        .doOnError { error ->
                            log.warn("Failed to commit dataSource: {}", dsName, error)
                        }
                )
            }

            chain.onErrorResume { error ->
                log.warn("Commit failed, rolling back all {} connections", connList.size, error)
                // If any commit fails, rollback all
                rollbackAll().then(Mono.error(error))
            }.doOnSuccess {
                log.debug("All {} connections committed successfully", connList.size)
            }
        }
    }

    fun rollbackAll(): Mono<Void> {
        return Mono.defer {
            if (connections.isEmpty()) {
                log.debug("No connections to rollback")
                return@defer Mono.empty()
            }

            val dataSourceNames = synchronized(connections) { connections.keys.toList() }
            val connList = synchronized(connections) { connections.values.toList() }

            log.warn("Rolling back {} connections: {}", connList.size, dataSourceNames)

            // Rollback all connections, ignore errors
            var chain = Mono.empty<Void>()
            dataSourceNames.forEachIndexed { index, dsName ->
                chain = chain.then(
                    Mono.from(connList[index].rollbackTransaction())
                        .doOnSuccess {
                            log.warn("Successfully rolled back dataSource: {}", dsName)
                        }
                        .onErrorResume { error ->
                            log.warn("Failed to rollback dataSource: {} (ignored)", dsName, error)
                            Mono.empty()
                        }
                )
            }
            chain.doOnSuccess {
                log.warn("All {} connections rolled back", connList.size)
            }
        }
    }

    fun closeAll(): Mono<Void> {
        return Mono.defer {
            if (connections.isEmpty()) {
                log.debug("No connections to close")
                return@defer Mono.empty()
            }

            val dataSourceNames = synchronized(connections) { connections.keys.toList() }
            val connList = synchronized(connections) { connections.values.toList() }

            log.debug("Closing {} connections: {}", connList.size, dataSourceNames)

            var chain = Mono.empty<Void>()
            dataSourceNames.forEachIndexed { index, dsName ->
                chain = chain.then(
                    Mono.from(connList[index].close())
                        .doOnSuccess {
                            log.debug("Successfully closed dataSource: {}", dsName)
                        }
                        .onErrorResume { error ->
                            log.debug("Failed to close dataSource: {} (ignored)", dsName, error)
                            Mono.empty()
                        }
                )
            }

            chain.doFinally {
                synchronized(connections) {
                    connections.clear()
                }
                log.debug("All connections closed and cleared")
            }
        }
    }

    fun isTransactionActive(): Boolean = transactionActive

    fun getConnectionCount(): Int = synchronized(connections) { connections.size }
}
