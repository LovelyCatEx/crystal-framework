/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.interceptor.SqlStatementInterceptor
import com.lovelycatv.vertex.log.logger
import net.sf.jsqlparser.statement.Statement
import org.springframework.core.Ordered

class R2dbcShardingSqlInterceptor(
    private val router: R2dbcShardingRouter,
    private val logDecisions: Boolean,
) : SqlStatementInterceptor {
    private val logger = logger()

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    override fun intercept(statement: Statement, tableName: String?): Statement {
        val decision = router.resolve(tableName)

        if (logDecisions) {
            logger.info(
                "SQL route decision: table=${decision.tableName ?: "<unknown>"}, " +
                    "shardingColumn=${decision.shardingColumn ?: "<none>"}, " +
                    "rule=${decision.ruleName ?: "<none>"}, " +
                    "dataSource=${decision.dataSourceName}, " +
                    "statementType=${statement.javaClass.simpleName}",
            )
        }

        return statement
    }
}
