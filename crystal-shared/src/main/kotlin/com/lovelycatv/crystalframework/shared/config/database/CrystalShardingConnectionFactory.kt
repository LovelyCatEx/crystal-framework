package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

/**
 * Crystal Framework 分片感知连接工厂。
 *
 * 返回虚拟连接 [CrystalShardingConnection]，不绑定任何真实物理连接。真实连接的获取推迟到
 * [io.r2dbc.spi.Statement.execute] 时，此时已有完整的 SQL 和绑定参数，可以精确路由到目标
 * 数据源和真实表。
 *
 * 这是 Crystal Framework 分库分表的入口，替代了之前的手动 Reactor Context 路由方式。规则配置
 * 在 [R2dbcShardingRule] 中声明，算法由用户通过 [ShardingAlgorithm] 接口注入。
 */
class CrystalShardingConnectionFactory(
    private val poolRegistry: R2dbcConnectionPoolRegistry,
    private val shardingRuleRegistry: R2dbcShardingRuleRegistry,
) : ConnectionFactory {

    override fun create(): Publisher<Connection> {
        return Mono.just(CrystalShardingConnection(poolRegistry, shardingRuleRegistry))
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        return poolRegistry.require(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME).metadata
    }
}
