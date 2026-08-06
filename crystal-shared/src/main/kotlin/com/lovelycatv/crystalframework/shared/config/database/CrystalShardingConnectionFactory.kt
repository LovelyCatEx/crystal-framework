package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

/**
 * Crystal Framework sharding-aware connection factory.
 *
 * Returns virtual connection [CrystalShardingConnection], does not bind to any real physical connection.
 * Real connection acquisition is deferred until [io.r2dbc.spi.Statement.execute], when complete SQL
 * and bound parameters are available for accurate routing to target data source and real table.
 *
 * This is the entry point for Crystal Framework's database sharding, replacing the previous manual
 * Reactor Context routing approach. Rules are declared in [R2dbcShardingRule], algorithms are
 * injected by users via [ShardingAlgorithm] interface.
 */
class CrystalShardingConnectionFactory(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val shardingRuleRegistry: R2dbcShardingRuleRegistry,
) : ConnectionFactory {

    override fun create(): Publisher<Connection> {
        return Mono.just(
            CrystalShardingConnection(
                poolRegistry = poolRegistry,
                shardingRuleRegistry = shardingRuleRegistry,
            )
        )
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        return poolRegistry.require(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME).metadata
    }
}
