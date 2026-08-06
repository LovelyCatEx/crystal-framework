package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient

@Configuration(proxyBeanMethods = false)
@EnableR2dbcRepositories(
    basePackages = ["com.lovelycatv.crystalframework"],
    entityOperationsRef = "r2dbcEntityTemplate",
)
class R2dbcRepositoryConfig {
    @Bean
    fun r2dbcEntityTemplate(
        databaseClient: DatabaseClient,
        @Qualifier("connectionFactory") connectionFactory: ConnectionFactory,
        converter: R2dbcConverter,
    ): R2dbcEntityTemplate {
        return R2dbcEntityTemplate(
            databaseClient,
            DialectResolver.getDialect(connectionFactory),
            converter,
        )
    }
}
