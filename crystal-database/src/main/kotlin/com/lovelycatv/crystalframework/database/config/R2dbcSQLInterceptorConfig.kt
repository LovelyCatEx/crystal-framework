/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database.config

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
