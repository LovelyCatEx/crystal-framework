package com.lovelycatv.crystalframework.database.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager

@Configuration(proxyBeanMethods = false)
class R2dbcTransactionConfig {
    @Bean(name = ["transactionManager"])
    fun transactionManager(
        @Qualifier("connectionFactory") connectionFactory: ConnectionFactory,
    ): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}
