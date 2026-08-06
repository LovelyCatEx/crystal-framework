package com.lovelycatv.crystalframework.shared.config.database

import com.lovelycatv.vertex.log.logger
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

/**
 * Routing-aware [ConnectionFactory] that selects the appropriate [ConnectionPool] based on
 * a data source name key read from the Reactor Context.
 *
 * - Reads [R2dbcDataSourceContext.KEY] from the current Reactor Context
 * - Falls back to [defaultDataSourceName] if no key is set or the key is unknown
 * - Wraps each connection in [DelegatedR2dbcConnection] to preserve SQL interceptor chain
 *
 * This is the foundation for multi-data-source routing: callers must set the routing key
 * via [R2dbcDataSourceContext.write] or [R2dbcDataSourceContext.writeForTable] before
 * executing queries.
 */
class R2dbcRoutingConnectionFactory(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val defaultDataSourceName: String,
) : ConnectionFactory {
    private val logger = logger()

    init {
        require(defaultDataSourceName.isNotBlank()) {
            "R2dbcRoutingConnectionFactory: defaultDataSourceName must not be blank"
        }
        poolRegistry.require(defaultDataSourceName) // Validate default exists
    }

    override fun create(): Publisher<out Connection> {
        return Mono.deferContextual { ctx ->
            val requestedDataSource = R2dbcDataSourceContext.current(ctx)
            val targetDataSource = requestedDataSource?.trim()?.takeIf(String::isNotEmpty)
                ?: defaultDataSourceName

            val pool = poolRegistry.get(targetDataSource)
                ?: run {
                    logger.warn(
                        "R2dbcRoutingConnectionFactory: unknown data source '{}', falling back to '{}'",
                        targetDataSource,
                        defaultDataSourceName,
                    )
                    poolRegistry.require(defaultDataSourceName)
                }

            if (logger.isDebugEnabled && requestedDataSource != null) {
                logger.debug(
                    "R2dbcRoutingConnectionFactory: routing to data source '{}'",
                    targetDataSource,
                )
            }

            Mono.from(pool.create())
                .map { connection -> DelegatedR2dbcConnection(connection) }
        }
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        // Return metadata from the default pool
        return poolRegistry.require(defaultDataSourceName).metadata
    }
}
