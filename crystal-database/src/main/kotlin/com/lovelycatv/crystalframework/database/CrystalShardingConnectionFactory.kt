package com.lovelycatv.crystalframework.database

import com.lovelycatv.crystalframework.database.constants.R2dbcDataSourceConstants
import com.lovelycatv.crystalframework.database.sharding.R2dbcShardingRuleRegistry
import com.lovelycatv.crystalframework.shared.config.observability.ApmParentSpan
import com.lovelycatv.crystalframework.shared.config.observability.ApmTraceHeaders
import com.lovelycatv.crystalframework.shared.config.observability.DistributedTransactionLabel
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
 * Reactor Context routing approach. Rules are declared in [com.lovelycatv.crystalframework.database.sharding.R2dbcShardingRule], algorithms are
 * injected by users via [com.lovelycatv.crystalframework.database.sharding.ShardingAlgorithm] interface.
 */
class CrystalShardingConnectionFactory(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val shardingRuleRegistry: R2dbcShardingRuleRegistry,
) : ConnectionFactory {

    override fun create(): Publisher<Connection> {
        return Mono.deferContextual { context ->
            Mono.just(
                CrystalShardingConnection(
                    poolRegistry = poolRegistry,
                    shardingRuleRegistry = shardingRuleRegistry,
                    traceHeaders = context.getOrDefault(ApmTraceHeaders::class.java, ApmTraceHeaders(emptyMap()))!!.values,
                    transactionLabel = context.getOrDefault(
                        DistributedTransactionLabel::class.java,
                        DistributedTransactionLabel.DEFAULT,
                    )!!,
                    apmParentSpan = if (context.hasKey(ApmParentSpan::class.java))
                        context.get<ApmParentSpan>(ApmParentSpan::class.java).span
                    else null,
                )
            )
        }
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        return poolRegistry.require(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME).metadata
    }
}
