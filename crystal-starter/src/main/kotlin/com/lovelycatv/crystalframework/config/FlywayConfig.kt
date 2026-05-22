package com.lovelycatv.crystalframework.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class FlywayConfig {

    @Bean
    fun flywayMigrationExecutor(environment: Environment): BeanFactoryPostProcessor {
        return BeanFactoryPostProcessor { _: ConfigurableListableBeanFactory ->
            val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

            val url = environment.getProperty("spring.datasource.url")
                ?: environment.resolveRequiredPlaceholders(
                    $$"jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DATABASE}"
                )
            val username = environment.getProperty("spring.datasource.username")
                ?: environment.getProperty("POSTGRES_USERNAME")
            val password = environment.getProperty("spring.datasource.password")
                ?: environment.getProperty("POSTGRES_PASSWORD")

            logger.info("Flyway: Starting database migration...")

            val flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()

            val result = flyway.migrate()

            logger.info("Flyway: Migration completed. Applied ${result.migrationsExecuted} migration(s). Current version: ${result.targetSchemaVersion}")
        }
    }
}
