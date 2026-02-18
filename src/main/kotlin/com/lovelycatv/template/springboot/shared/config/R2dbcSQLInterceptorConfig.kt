package com.lovelycatv.template.springboot.shared.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient

@Configuration
class R2dbcSQLInterceptorConfig {
    @Bean
    fun databaseClient(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(DelegatedR2dbcConnectionFactory(connectionFactory))
            .executeFunction { statement ->
                statement.execute()
            }
            .build()
    }
}