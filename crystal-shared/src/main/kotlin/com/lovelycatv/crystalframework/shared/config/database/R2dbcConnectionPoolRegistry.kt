package com.lovelycatv.crystalframework.shared.config.database

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.vertex.log.logger
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.pool.PoolMetrics
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import io.r2dbc.spi.ValidationDepth
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder

/**
 * Creates and holds one [ConnectionPool] per data source declaration.
 * The primary pool is registered from the existing [rawR2dbcConnectionPool] bean;
 * additional data sources are built dynamically from [CrystalFrameworkConfiguration.Database.DataSource].
 */
class R2dbcConnectionPoolRegistry(
    primaryPool: ConnectionPool,
    additionalDataSources: List<CrystalFrameworkConfiguration.Database.DataSource>,
) {
    private val logger = logger()
    private val poolsByName = linkedMapOf<String, ConnectionPool>()

    init {
        // Register primary pool
        poolsByName[R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME] = primaryPool
        logger.info(
            "Registered primary connection pool: {} (initial={}, max={})",
            R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME,
            primaryPool.metrics.map(PoolMetrics::acquiredSize).orElse(-1),
            primaryPool.metrics.map(PoolMetrics::getMaxAllocatedSize).orElse(-1),
        )

        // Build additional pools
        additionalDataSources.forEach { ds ->
            val name = ds.name.trim()
            require(name.isNotEmpty()) { "R2dbcConnectionPoolRegistry: data source name must not be blank" }
            require(ds.url.isNotBlank()) { "R2dbcConnectionPoolRegistry: data source '$name' url must not be blank" }

            if (poolsByName.containsKey(name)) {
                throw IllegalStateException("R2dbcConnectionPoolRegistry: duplicate data source name '$name'")
            }

            val pool = buildConnectionPool(ds)
            poolsByName[name] = pool
            logger.info(
                "Registered additional connection pool: {} (initial={}, max={})",
                name,
                ds.pool.initialSize,
                ds.pool.maxSize,
            )
        }
    }

    fun get(name: String): ConnectionPool? = poolsByName[name.trim()]

    fun require(name: String): ConnectionPool =
        get(name) ?: throw IllegalArgumentException("R2dbcConnectionPoolRegistry: unknown pool '$name'")

    fun names(): Set<String> = poolsByName.keys.toSet()

    private fun buildConnectionPool(ds: CrystalFrameworkConfiguration.Database.DataSource): ConnectionPool {
        val parsedOptions = ConnectionFactoryOptions.parse(ds.url)
        val options = parsedOptions.mutate()

        ds.username
            .takeIf(String::isNotBlank)
            ?.takeIf { parsedOptions.getValue(ConnectionFactoryOptions.USER) == null }
            ?.let { options.option(ConnectionFactoryOptions.USER, it) }

        ds.password
            .takeIf(String::isNotBlank)
            ?.takeIf { parsedOptions.getValue(ConnectionFactoryOptions.PASSWORD) == null }
            ?.let { options.option(ConnectionFactoryOptions.PASSWORD, it) }

        val driverConnectionFactory = ConnectionFactoryBuilder
            .withOptions(options)
            .build()

        val poolConfig = ds.pool
        val builder = ConnectionPoolConfiguration.builder(driverConnectionFactory)
            .initialSize(poolConfig.initialSize)
            .minIdle(poolConfig.minIdle)
            .maxSize(poolConfig.maxSize)
            .maxIdleTime(poolConfig.maxIdleTime)
            .acquireRetry(poolConfig.acquireRetry)
            .validationQuery(poolConfig.validationQuery)
            .validationDepth(ValidationDepth.valueOf(poolConfig.validationDepth))

        poolConfig.maxAcquireTime?.let(builder::maxAcquireTime)
        poolConfig.maxCreateConnectionTime?.let(builder::maxCreateConnectionTime)
        poolConfig.maxLifeTime?.let(builder::maxLifeTime)
        poolConfig.maxValidationTime?.let(builder::maxValidationTime)

        return ConnectionPool(builder.build())
    }
}
