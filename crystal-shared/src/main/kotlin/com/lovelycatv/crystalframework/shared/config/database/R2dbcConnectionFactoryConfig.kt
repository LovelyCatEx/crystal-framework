package com.lovelycatv.crystalframework.shared.config.database

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration(proxyBeanMethods = false)
class R2dbcConnectionFactoryConfig {
    @Bean(name = ["connectionFactory"])
    @Primary
    fun connectionFactory(
        poolRegistry: R2dbcConnectionPoolRegistry,
        configuration: CrystalFrameworkConfiguration,
    ): ConnectionFactory {
        return R2dbcRoutingConnectionFactory(
            poolRegistry,
            configuration.database.defaultDataSource,
        )
    }
}
