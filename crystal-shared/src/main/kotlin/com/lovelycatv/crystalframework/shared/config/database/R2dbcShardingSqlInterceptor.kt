package com.lovelycatv.crystalframework.shared.config.database

import com.lovelycatv.crystalframework.shared.config.SqlStatementInterceptor
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
