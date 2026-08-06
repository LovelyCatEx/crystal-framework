package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(R2dbcProperties::class)
class R2dbcConnectionPoolConfig {
    @Bean(name = ["rawR2dbcConnectionPool"], destroyMethod = "dispose")
    fun rawR2dbcConnectionPool(
        properties: R2dbcProperties,
    ): ConnectionPool {
        val url = requireNotNull(properties.url) { "spring.r2dbc.url must be configured" }
        val parsedOptions = ConnectionFactoryOptions.parse(url)
        val options = parsedOptions.mutate()
        properties.username
            ?.takeIf(String::isNotBlank)
            ?.takeIf { parsedOptions.getValue(ConnectionFactoryOptions.USER) == null }
            ?.let { options.option(ConnectionFactoryOptions.USER, it) }
        properties.password
            ?.takeIf { parsedOptions.getValue(ConnectionFactoryOptions.PASSWORD) == null }
            ?.let { options.option(ConnectionFactoryOptions.PASSWORD, it) }
        properties.properties.forEach { (key, value) ->
            options.option(Option.valueOf(key), value)
        }
        val driverConnectionFactory = ConnectionFactoryBuilder
            .withOptions(options)
            .build()
        val pool = properties.pool
        val builder = ConnectionPoolConfiguration.builder(driverConnectionFactory)
            .maxIdleTime(pool.maxIdleTime)
            .acquireRetry(pool.acquireRetry)
            .initialSize(pool.initialSize)
            .minIdle(pool.minIdle)
            .maxSize(pool.maxSize)
            .validationDepth(pool.validationDepth)

        pool.maxLifeTime?.let(builder::maxLifeTime)
        pool.maxAcquireTime?.let(builder::maxAcquireTime)
        pool.maxCreateConnectionTime?.let(builder::maxCreateConnectionTime)
        pool.maxValidationTime?.let(builder::maxValidationTime)
        pool.validationQuery?.let(builder::validationQuery)

        return ConnectionPool(builder.build())
    }
}
