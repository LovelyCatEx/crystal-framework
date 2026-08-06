package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.ConnectionFactory

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient

@Configuration(proxyBeanMethods = false)
class R2dbcSQLInterceptorConfig {
    @Bean
    fun databaseClient(
        @Qualifier("connectionFactory") connectionFactory: ConnectionFactory,
    ): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .executeFunction { statement ->
                statement.execute()
            }
            .build()
    }
}
