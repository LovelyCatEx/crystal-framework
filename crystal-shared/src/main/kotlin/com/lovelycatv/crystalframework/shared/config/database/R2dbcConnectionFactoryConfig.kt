package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration(proxyBeanMethods = false)
class R2dbcConnectionFactoryConfig {
    @Bean(name = ["connectionFactory"])
    @Primary
    fun connectionFactory(
        @Qualifier("rawR2dbcConnectionPool") pool: ConnectionPool,
    ): ConnectionFactory {
        return DelegatedR2dbcConnectionFactory(pool)
    }
}
