package com.lovelycatv.crystalframework.shared.config

import io.r2dbc.pool.ConnectionPool
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.PreparedOperation
import org.springframework.r2dbc.core.binding.BindTarget
import java.util.function.Supplier

@Configuration
class R2dbcSQLInterceptorConfig {
    @Bean
    fun databaseClient(connectionFactory: ConnectionPool): DatabaseClient {
        val databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .executeFunction { statement ->
                statement.execute()
            }
            .build()

        return object : DatabaseClient by databaseClient {
            override fun sql(sql: String): DatabaseClient.GenericExecuteSpec {
                return databaseClient.sql(CrystalFrameworkSQLModifier.processSql(sql))
            }

            override fun sql(sqlSupplier: Supplier<String>): DatabaseClient.GenericExecuteSpec {
                val delegate = sqlSupplier as PreparedOperation<*>

                return databaseClient.sql(object : PreparedOperation<Any> {
                    override fun getSource(): Any = delegate.source

                    override fun bindTo(target: BindTarget) {
                        delegate.bindTo(target)
                    }

                    override fun toQuery(): String {
                        return CrystalFrameworkSQLModifier.processSql(delegate.toQuery())
                    }
                })
            }
        }
    }
}