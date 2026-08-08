package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.R2dbcConnectionPoolRegistry
import com.lovelycatv.crystalframework.database.constants.R2dbcDataSourceConstants
import com.lovelycatv.crystalframework.database.R2dbcDataSourceDeclaration
import com.lovelycatv.crystalframework.database.R2dbcDataSourceRegistry
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import io.r2dbc.pool.ConnectionPool
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class R2dbcShardingConfiguration {
    @Bean
    fun r2dbcDataSourceRegistry(
        configuration: CrystalFrameworkConfiguration,
        r2dbcProperties: R2dbcProperties,
    ): R2dbcDataSourceRegistry {
        val primaryUrl = requireNotNull(r2dbcProperties.url) { "spring.r2dbc.url must be configured" }
        val primary = R2dbcDataSourceDeclaration(
            name = R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME,
            url = primaryUrl,
            username = r2dbcProperties.username.orEmpty(),
            password = r2dbcProperties.password.orEmpty(),
        )
        val additional = configuration.database.dataSources.orEmpty().map {
            R2dbcDataSourceDeclaration(
                name = it.name,
                url = it.url,
                username = it.username,
                password = it.password,
            )
        }
        val registry = R2dbcDataSourceRegistry(listOf(primary) + additional)
        registry.require(configuration.database.defaultDataSource)
        return registry
    }

    @Bean
    fun r2dbcConnectionPoolRegistry(
        @Qualifier("rawR2dbcConnectionPool") primaryPool: ConnectionPool,
        configuration: CrystalFrameworkConfiguration,
    ): R2dbcConnectionPoolRegistry {
        return R2dbcConnectionPoolRegistry(
            primaryPool,
            configuration.database.dataSources.toList(),
        )
    }

    @Bean
    fun r2dbcShardingRuleRegistry(
        components: ObjectProvider<R2dbcShardingRuleComponent>,
        dataSources: R2dbcDataSourceRegistry,
    ): R2dbcShardingRuleRegistry {
        return R2dbcShardingRuleRegistry(components.orderedStream().toList(), dataSources)
    }

    @Bean
    fun r2dbcShardingSqlInterceptor(
        configuration: CrystalFrameworkConfiguration,
        router: R2dbcShardingRouter,
    ): R2dbcShardingSqlInterceptor {
        return R2dbcShardingSqlInterceptor(
            router,
            configuration.database.routing.isLogDecisions,
        )
    }

    @Bean
    fun r2dbcShardingRouter(
        configuration: CrystalFrameworkConfiguration,
        rules: R2dbcShardingRuleRegistry,
    ): R2dbcShardingRouter {
        val defaultDataSource = configuration.database.defaultDataSource.trim()
        require(defaultDataSource.isNotEmpty()) {
            "crystalframework.database.default-data-source must not be blank"
        }
        return R2dbcShardingRouter(defaultDataSource, rules)
    }
}
