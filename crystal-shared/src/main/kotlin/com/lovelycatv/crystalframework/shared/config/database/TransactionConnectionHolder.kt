package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.Connection
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

/**
 * Manages multiple database connections for distributed transaction.
 *
 * Workflow:
 * 1. markTransactionStart() - mark transaction started
 * 2. getOrCreateConnection(dataSource) - lazy acquire connection and begin transaction
 * 3. All connections remain open with active transactions
 * 4. commitAll() - commit all connections, rollback all if any fails
 * 5. rollbackAll() - rollback all connections on error
 *
 * Note: This is NOT true XA - if one connection commits successfully but another fails,
 * data inconsistency can occur. For true ACID across shards, integrate Seata or XA coordinator.
 */
class TransactionConnectionHolder(
    private val poolRegistry: R2dbcConnectionPoolRegistry
) {
    private val connections = mutableMapOf<String, Connection>()

    @Volatile
    private var transactionActive = false

    companion object {
        private val log = LoggerFactory.getLogger(TransactionConnectionHolder::class.java)
    }

    fun markTransactionStart() {
        transactionActive = true
        log.debug("Distributed transaction started")
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
