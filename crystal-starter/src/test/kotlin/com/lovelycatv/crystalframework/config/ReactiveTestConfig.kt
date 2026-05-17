package com.lovelycatv.crystalframework.config

import com.lovelycatv.vertex.log.logger
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager

@TestConfiguration
class ReactiveTestConfig : InitializingBean {
    private val logger = logger()

    @Bean
    fun reactiveTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    override fun afterPropertiesSet() {
        logger.info("ReactiveTestConfig afterPropertiesSet()")
    }
}